package ai.tecton.client.transport;

import ai.tecton.client.TectonClientOptions;
import ai.tecton.client.exceptions.TectonClientException;
import ai.tecton.client.exceptions.TectonErrorMessage;
import ai.tecton.client.exceptions.TectonServiceException;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

public class TectonHttpClient {

  private HttpUrl url;
  private final String apiKey;
  private final OkHttpClient client;
  private final AtomicBoolean isClosed;
  private static final String API_KEY_PREFIX = "Tecton-key ";
  private static final int TIMEOUT = 5;
  private static Moshi moshi = new Moshi.Builder().build();
  private static final JsonAdapter<ErrorResponseJson> jsonAdapter =
      moshi.adapter(ErrorResponseJson.class);

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
    try {
      Response response = call.execute();
      if (!response.isSuccessful()) {
        parseErrorResponse(response);
      }
      return new HttpResponse(response);
    } catch (Exception e) {
      throw new TectonServiceException(e.getMessage());
    }
  }

  public List<HttpResponse> performParallelRequests(
      String endpoint, HttpMethod method, List<String> requestBodyList, Duration timeout)
      throws TectonClientException {

    int numCalls = requestBodyList.size();
    List<Call> calls = new ArrayList<>(numCalls);
    List<HttpResponse> httpResponses = new ArrayList<>(numCalls);
    CountDownLatch countDownLatch = new CountDownLatch(numCalls);

    Callback callback =
        new Callback() {
          @Override
          public void onFailure(@NotNull Call call, @NotNull IOException e) {
            throw new TectonClientException(TectonErrorMessage.ERROR_RESPONSE);
          }

          @Override
          public void onResponse(@NotNull Call call, @NotNull Response response)
              throws IOException {
            try {
              if (!response.isSuccessful()) {
                parseErrorResponse(response);
              }
              httpResponses.add(calls.indexOf(call), new HttpResponse(response));
            } catch (Exception e) {
              throw new TectonServiceException(e.getMessage());
            }
          }
        };

    requestBodyList.forEach(
        requestBody -> {
          HttpRequest httpRequest =
              new HttpRequest(url.url().toString(), endpoint, method, apiKey, requestBody);
          Request request = buildRequestWithDefaultHeaders(httpRequest);
          Call call = client.newCall(request);
          calls.add(call);
          call.enqueue(callback);
        });

    try {
      countDownLatch.await(timeout.toMillis(), TimeUnit.MILLISECONDS);
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
    return client.dispatcher().getMaxRequests();
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

  private static void parseErrorResponse(Response response) {
    // Parse error response and throw TectonServiceException
    String errorMessage = response.message();
    try {
      ErrorResponseJson errorResponseJson = jsonAdapter.fromJson(response.body().string());
      errorMessage = errorResponseJson.message;
    } catch (Exception e) {
      throw new TectonClientException(TectonErrorMessage.INVALID_RESPONSE_FORMAT);
    }
    throw new TectonServiceException(
        String.format(TectonErrorMessage.ERROR_RESPONSE, response.code(), errorMessage));
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

  static class ErrorResponseJson {
    String error;
    int code;
    String message;
  }
}
