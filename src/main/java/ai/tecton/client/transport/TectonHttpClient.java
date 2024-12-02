package ai.tecton.client.transport;

import ai.tecton.client.TectonClientOptions;
import ai.tecton.client.exceptions.TectonClientException;
import ai.tecton.client.exceptions.TectonErrorMessage;
import ai.tecton.client.exceptions.TectonServiceException;
import ai.tecton.client.version.Version;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

public class TectonHttpClient {

  private HttpUrl url;
  private final String apiKey;
  private final OkHttpClient client;
  private final AtomicBoolean isClosed;
  private static final String API_KEY_PREFIX = "Tecton-key ";
  private static final String USER_AGENT_STRING_PREFIX = "tecton-http-java-client ";

  private static final Map<String, String> defaultHeaders =
      new HashMap<String, String>() {
        {
          put(HttpHeader.CONTENT_TYPE.getName(), MediaType.APPLICATION_JSON.getName());
          put(HttpHeader.ACCEPT.getName(), MediaType.APPLICATION_JSON.getName());
          put(HttpHeader.USER_AGENT.getName(), USER_AGENT_STRING_PREFIX + Version.VERSION);
        }
      };

  public TectonHttpClient(String url, String apiKey, TectonClientOptions tectonClientOptions) {
    validateUrl(url);
    validateApiKey(apiKey);
    this.apiKey = apiKey;
    Dispatcher dispatcher = new Dispatcher();
    dispatcher.setMaxRequestsPerHost(tectonClientOptions.getMaxParallelRequests());
    dispatcher.setMaxRequests(tectonClientOptions.getMaxParallelRequests());

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

  public TectonHttpClient(String url, String apiKey, OkHttpClient httpClient) {
    validateUrl(url);
    if (apiKey != null) {
      validateApiKey(apiKey);
    }
    this.client = httpClient;
    this.apiKey = apiKey;
    this.isClosed = new AtomicBoolean(false);
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
    // Initialize response list
    ParallelCallHandler parallelCallHandler = new ParallelCallHandler(requestBodyList.size());

    // Map request body to OkHttp Request
    // ordering of requests is maintained
    List<Request> requestList =
        requestBodyList.stream()
            .map(
                requestBody ->
                    new HttpRequest(url.url().toString(), endpoint, method, apiKey, requestBody))
            .map(this::buildRequestWithDefaultHeaders)
            .collect(Collectors.toList());

    // Initialize a countdown latch for numberOfCalls.
    CountDownLatch countDownLatch = new CountDownLatch(requestBodyList.size());

    Callback callback =
        new Callback() {
          @Override
          public void onFailure(Call call, IOException e) {
            // On timeout, executor rejects all pending calls. This could lead to an
            // InterruptedIOException for in-flight calls, which is expected.
            // Only log failures for other call failures such as network issues
            if (!(e instanceof InterruptedIOException)) {
              parallelCallHandler.logCallFailure(e.getMessage());
            }
          }

          @Override
          public void onResponse(Call call, Response response) {
            try (ResponseBody responseBody = response.body()) {
              // Add response to corresponding index
              parallelCallHandler.set(
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
    requestList.forEach(
        request -> {
          client.newCall(request).enqueue(callback);
        });

    // Wait until A) all calls have completed or B) specified timeout has elapsed
    try {
      boolean completedAllCalls = countDownLatch.await(timeout.toMillis(), TimeUnit.MILLISECONDS);
      if (!parallelCallHandler.failureMessageList.isEmpty()) {
        throw new TectonClientException(
            String.format(
                TectonErrorMessage.CALL_FAILURE, parallelCallHandler.failureMessageList.get(0)));
      }
      return parallelCallHandler.responseList;
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

  private void validateApiKey(String apiKey) {
    try {
      Validate.notEmpty(apiKey);
    } catch (Exception e) {
      throw new TectonClientException(TectonErrorMessage.INVALID_KEY);
    }
  }

  private void validateUrl(String url) {
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
    AUTHORIZATION("Authorization"),
    USER_AGENT("User-Agent");

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

  static class ParallelCallHandler {
    List<HttpResponse> responseList;
    List<String> failureMessageList;

    ParallelCallHandler(int numberOfCalls) {
      this.responseList = new ArrayList<>(Collections.nCopies(numberOfCalls, null));
      this.failureMessageList = new ArrayList<>(numberOfCalls);
    }

    void set(int index, HttpResponse httpResponse) {
      this.responseList.set(index, httpResponse);
    }

    void logCallFailure(String failureMessage) {
      // Log all call failure messages. Currently we only use one but this can be useful for error
      // handling per call in future
      this.failureMessageList.add(failureMessage);
    }
  }
}
