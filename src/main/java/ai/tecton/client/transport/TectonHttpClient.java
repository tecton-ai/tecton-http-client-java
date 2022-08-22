package ai.tecton.client.transport;

import ai.tecton.client.TectonClientOptions;
import ai.tecton.client.exceptions.TectonClientException;
import ai.tecton.client.exceptions.TectonErrorMessage;
import ai.tecton.client.exceptions.TectonServiceException;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

public class TectonHttpClient {

  private HttpUrl url;
  private final String apiKey;
  private final OkHttpClient client;
  private final AtomicBoolean isClosed;
  private static final String API_KEY_PREFIX = "Tecton-key ";

  private static final Map<String, String> defaultHeaders =
      new HashMap<String, String>() {
        {
          put(HttpHeader.CONTENT_TYPE.getName(), MediaType.APPLICATION_JSON.getName());
          put(HttpHeader.ACCEPT.getName(), MediaType.APPLICATION_JSON.getName());
        }
      };

  public TectonHttpClient(String url, String apiKey, TectonClientOptions tectonClientOptions) {
    validateClientParameters(url, apiKey);
    this.apiKey = apiKey;
    Dispatcher dispatcher = new Dispatcher();
    dispatcher.setMaxRequestsPerHost(tectonClientOptions.getMaxParallelRequests());

    OkHttpClient.Builder builder =
        new OkHttpClient.Builder()
            .readTimeout(tectonClientOptions.getReadTimeout().getSeconds(), TimeUnit.SECONDS)
            .connectTimeout(tectonClientOptions.getConnectTimeout().getSeconds(), TimeUnit.SECONDS)
            .dispatcher(dispatcher);
    ConnectionPool connectionPool =
        new ConnectionPool(
            tectonClientOptions.getMaxIdleConnections(),
            tectonClientOptions.getKeepAliveDuration().getSeconds(),
            TimeUnit.SECONDS);
    builder.connectionPool(connectionPool);
    client = builder.build();
    isClosed = new AtomicBoolean(false);
  }

  public void close() {
    if (isClosed.compareAndSet(false, true)) {
      client.dispatcher().executorService().shutdown();
      client.connectionPool().evictAll();
    }
  }

  public boolean isClosed() {
    return isClosed.get();
  }

  public HttpResponse performRequest(String endpoint, HttpMethod method, String requestBody) {
    if (this.isClosed()) {
      throw new TectonClientException(TectonErrorMessage.CLOSED_CLIENT);
    }
    HttpRequest httpRequest =
        new HttpRequest(url.url().toString(), endpoint, method, apiKey, requestBody);
    Request request = buildRequestWithDefaultHeaders(httpRequest);
    Call call = client.newCall(request);
    try (Response response = call.execute()) {
      return new HttpResponse(response);
    } catch (Exception e) {
      throw new TectonClientException(e.getMessage());
    }
  }

  public List<HttpResponse> performParallelRequests(
      String endpoint, HttpMethod method, List<String> requestBodyList, Duration timeout)
      throws TectonClientException {
    if (this.isClosed()) {
      throw new TectonClientException(TectonErrorMessage.CLOSED_CLIENT);
    }

    // Initialize response list
    int numberOfCalls = requestBodyList.size();
    List<HttpResponse> httpResponses = new ArrayList<>(Collections.nCopies(numberOfCalls, null));

    // Map request body to OkHttp Request
    // ordering of requests is maintained
    List<Request> requestList =
        requestBodyList
            .parallelStream()
            .map(
                requestBody ->
                    new HttpRequest(url.url().toString(), endpoint, method, apiKey, requestBody))
            .map(this::buildRequestWithDefaultHeaders)
            .collect(Collectors.toList());

    // Initialize a countdown latch for numberOfCalls.
    CountDownLatch countDownLatch = new CountDownLatch(numberOfCalls);

    Callback callback =
        new Callback() {
          @Override
          public void onFailure(Call call, IOException e) {
            // Call failures could be due to a call being in-flight when timeout has elapsed
          }

          @Override
          public void onResponse(Call call, Response response) {
            try (ResponseBody responseBody = response.body()) {
              // Add response to corresponding index
              httpResponses.set(
                  requestList.indexOf(call.request()), new HttpResponse(response, responseBody));
            } catch (Exception e) {
              throw new TectonServiceException(e.getMessage());
            } finally {
              Objects.requireNonNull(response.body()).close();
              countDownLatch.countDown();
            }
          }
        };

    // Enqueue all calls
    requestList
        .parallelStream()
        .forEach(
            request -> {
              client.newCall(request).enqueue(callback);
            });

    // Wait until A) all calls have completed or B) specified timeout has elapsed
    try {
      boolean completedAllCalls = countDownLatch.await(timeout.toMillis(), TimeUnit.MILLISECONDS);
      return httpResponses;
    } catch (InterruptedException e) {
      throw new TectonClientException(e.getMessage());
    }
  }

  public Request buildRequestWithDefaultHeaders(HttpRequest httpRequest) {
    // Construct url
    Request.Builder requestBuilder = new Request.Builder().url(httpRequest.getUrl());

    // Add headers
    for (Map.Entry<String, String> header : defaultHeaders.entrySet()) {
      requestBuilder.header(header.getKey(), header.getValue());
    }
    String apiKeyHeader = StringUtils.join(API_KEY_PREFIX + httpRequest.getApiKey());
    requestBuilder.header(HttpHeader.AUTHORIZATION.getName(), apiKeyHeader);

    // Add request body
    switch (httpRequest.getMethod()) {
      case POST:
      default:
        {
          okhttp3.MediaType mediaType =
              okhttp3.MediaType.parse(MediaType.APPLICATION_JSON.getName());
          RequestBody requestBody = RequestBody.create(httpRequest.getJsonBody(), mediaType);
          requestBuilder.post(requestBody);
        }
    }
    return requestBuilder.build();
  }

  Duration getReadTimeout() {
    return Duration.ofMillis(client.readTimeoutMillis());
  }

  Duration getConnectTimeout() {
    return Duration.ofMillis(client.connectTimeoutMillis());
  }

  int getMaxParallelRequests() {
    return client.dispatcher().getMaxRequestsPerHost();
  }

  private void validateClientParameters(String url, String apiKey) {
    try {
      Validate.notEmpty(apiKey);
    } catch (Exception e) {
      throw new TectonClientException(TectonErrorMessage.INVALID_KEY);
    }

    try {
      Validate.notEmpty(url);
      this.url = HttpUrl.parse(url);
    } catch (Exception e) {
      throw new TectonClientException(TectonErrorMessage.INVALID_URL);
    }
  }

  public enum HttpMethod {
    GET,
    POST,
    PUT,
    DELETE;
  }

  enum HttpHeader {
    CONTENT_TYPE("Content-Type"),
    ACCEPT("Accept"),
    AUTHORIZATION("Authorization");

    private final String name;

    HttpHeader(String headerName) {
      this.name = headerName;
    }

    public String getName() {
      return name;
    }
  }

  enum MediaType {
    APPLICATION_JSON("application/json"),
    PLAIN_TEXT("text/plain");

    private final String name;

    MediaType(String name) {
      this.name = name;
    }

    public String getName() {
      return this.name;
    }
  }
}
