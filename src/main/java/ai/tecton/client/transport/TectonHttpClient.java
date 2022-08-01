package ai.tecton.client.transport;

import ai.tecton.client.TectonClientOptions;
import ai.tecton.client.exceptions.TectonClientException;
import ai.tecton.client.exceptions.TectonErrorMessage;
import ai.tecton.client.exceptions.TectonServiceException;
import ai.tecton.client.transport.benchmark.CallEventListener;
import ai.tecton.client.transport.benchmark.HttpMetrics;
import ai.tecton.client.transport.benchmark.OkhttpCallLog;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import okhttp3.*;
import okhttp3.internal.connection.RealCall;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

public class TectonHttpClient {

  private HttpUrl url;
  private final String apiKey;
  private OkHttpClient client = new OkHttpClient();
  private final AtomicBoolean isClosed;
  private static final String API_KEY_PREFIX = "Tecton-key ";
  private static final int TIMEOUT = 5;
  private final boolean DEBUG_MODE;

  private static final Map<String, String> defaultHeaders =
      new HashMap<String, String>() {
        {
          put(HttpHeader.CONTENT_TYPE.getName(), MediaType.APPLICATION_JSON.getName());
          put(HttpHeader.ACCEPT.getName(), MediaType.APPLICATION_JSON.getName());
        }
      };

  public TectonHttpClient(String url, String apiKey) {
    validateClientParameters(url, apiKey);
    this.apiKey = apiKey;
    client =
        client
            .newBuilder()
            .readTimeout(TIMEOUT, TimeUnit.SECONDS)
            .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
            .build();
    isClosed = new AtomicBoolean(false);
    this.DEBUG_MODE = false;
  }

  public TectonHttpClient(String url, String apiKey, TectonClientOptions tectonClientOptions) {
    validateClientParameters(url, apiKey);
    this.apiKey = apiKey;
    OkHttpClient.Builder builder =
        client
            .newBuilder()
            .readTimeout(tectonClientOptions.getReadTimeout().getSeconds(), TimeUnit.SECONDS)
            .connectTimeout(tectonClientOptions.getConnectTimeout().getSeconds(), TimeUnit.SECONDS);
    ConnectionPool connectionPool =
        new ConnectionPool(
            tectonClientOptions.getMaxIdleConnections(),
            tectonClientOptions.getKeepAliveDuration().getSeconds(),
            TimeUnit.SECONDS);
    builder.connectionPool(connectionPool);
    client = builder.build();
    isClosed = new AtomicBoolean(false);
    this.DEBUG_MODE = false;
  }

  public TectonHttpClient(String url, String apiKey, boolean debugMode) {
    validateClientParameters(url, apiKey);
    this.apiKey = apiKey;
    client =
        client
            .newBuilder()
            .readTimeout(TIMEOUT, TimeUnit.SECONDS)
            .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
            .eventListenerFactory(CallEventListener.MetricsEventListenerFactory)
            .build();
    isClosed = new AtomicBoolean(false);
    this.DEBUG_MODE = debugMode;
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
    RealCall call = new RealCall(client, request, false);
    long callId = 0;

    if (this.DEBUG_MODE) {
      CallEventListener metricsEventListener = (CallEventListener) call.getEventListener$okhttp();
      callId = metricsEventListener.getCallId();
    }
    HttpResponse httpResponse;
    try {
      Response response = call.execute();
      httpResponse = new HttpResponse(response);
      response.close();
      if (this.DEBUG_MODE && CallEventListener.callToMetricsMap.containsKey(callId)) {
        OkhttpCallLog callLog = CallEventListener.callToMetricsMap.get(callId);
        if (callLog.isValidCallLog()) httpResponse.setCallMetrics(new HttpMetrics(callLog));
      }
      return httpResponse;
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println(e.getMessage());
      throw new TectonServiceException(e.getMessage());
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

  public Duration getReadTimeout() {
    return Duration.ofMillis(client.readTimeoutMillis());
  }

  public Duration getConnectTimeout() {
    return Duration.ofMillis(client.connectTimeoutMillis());
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
