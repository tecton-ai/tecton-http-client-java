package ai.tecton.client.transport;

import ai.tecton.client.TectonClientOptions;
import ai.tecton.client.request.RequestConstants;
import ai.tecton.client.transport.TectonHttpClient.HttpMethod;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TectonHttpClientTest {
  private String url;
  private String endpoint;
  private String apiKey;
  private String body;
  TectonHttpClient httpClient;

  private static final String POST = "POST";

  private String baseUrlString;
  private static final String ERROR_RESPONSE = "Error Response Message";
  private static final HttpMethod method = HttpMethod.POST;

  @Before
  public void setup() throws IOException {
    url = "http://test-url.com";
    endpoint = "/api/v1/feature-service/get-features";
    apiKey = "12345";
    body = "{}";
    httpClient = new TectonHttpClient(url, apiKey, new TectonClientOptions.Builder().build());

    MockWebServer mockWebServer = new MockWebServer();
    mockWebServer.start();
    HttpUrl baseUrl = mockWebServer.url("");
    this.baseUrlString = baseUrl.url().toString();

    Dispatcher dispatcher =
        new Dispatcher() {
          @Override
          public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
            if (request.getBodySize() == 0) {
              return new MockResponse().setResponseCode(400).setBody(ERROR_RESPONSE);
            }
            // Randomly assign
            return new MockResponse().setResponseCode(200).setBody(request.getBody());
          }
        };
    mockWebServer.setDispatcher(dispatcher);
  }

  @Test
  public void testDefaultHttpClient() {
    TectonHttpClient httpClient =
        new TectonHttpClient(url, apiKey, new TectonClientOptions.Builder().build());
    Assert.assertEquals(2, httpClient.getConnectTimeout().getSeconds());
    Assert.assertEquals(2, httpClient.getReadTimeout().getSeconds());
    Assert.assertEquals(5, httpClient.getMaxParallelRequests());
    Assert.assertFalse(httpClient.isClosed());
  }

  @Test
  public void testDefaultTectonRequest() {
    HttpRequest httpRequest =
        new HttpRequest(url, endpoint, TectonHttpClient.HttpMethod.POST, apiKey, body);

    Request request = httpClient.buildRequestWithDefaultHeaders(httpRequest);
    Assert.assertNotNull(request);

    Assert.assertEquals(POST, request.method());
    Assert.assertEquals(
        request.headers().get(TectonHttpClient.HttpHeader.CONTENT_TYPE.getName()),
        TectonHttpClient.MediaType.APPLICATION_JSON.getName());
    Assert.assertEquals(
        TectonHttpClient.MediaType.APPLICATION_JSON.getName(),
        request.headers().get(TectonHttpClient.HttpHeader.ACCEPT.getName()));
    Assert.assertEquals(
        "Tecton-key " + apiKey,
        request.headers().get(TectonHttpClient.HttpHeader.AUTHORIZATION.getName()));
  }

  @Test
  public void testClientWithTectonOptions() {
    TectonClientOptions tectonClientOptions =
        new TectonClientOptions.Builder()
            .maxParallelRequests(20)
            .readTimeout(Duration.ofSeconds(10))
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    TectonHttpClient tectonHttpClient = new TectonHttpClient(url, apiKey, tectonClientOptions);

    Assert.assertEquals(10, tectonHttpClient.getReadTimeout().getSeconds());
    Assert.assertEquals(10, tectonHttpClient.getConnectTimeout().getSeconds());
    Assert.assertEquals(20, tectonHttpClient.getMaxParallelRequests());
  }

  @Test
  public void testParallelRequestsWithDefaultClient() {

    httpClient =
        new TectonHttpClient(
            this.baseUrlString, this.apiKey, new TectonClientOptions.Builder().build());
    // Prepare 10 requests and call client
    List<String> requestList = prepareRequests(10);
    List<HttpResponse> httpResponses =
        httpClient.performParallelRequests(endpoint, method, requestList, Duration.ofSeconds(60));

    List<String> responseList =
        httpResponses.stream()
            .map(httpResponse -> httpResponse.getResponseBody().get())
            .collect(Collectors.toList());

    // Verify request and response ordering
    Assert.assertEquals(requestList, responseList);
  }

  @Test
  public void testParallelRequestsWithPartialErrorResponses() {

    httpClient =
        new TectonHttpClient(
            this.baseUrlString, this.apiKey, new TectonClientOptions.Builder().build());
    // Prepare 10 valid requests and add 3 empty strings
    List<String> requestList = prepareRequests(10);
    requestList.addAll(Arrays.asList("", "", ""));
    List<HttpResponse> httpResponses =
        httpClient.performParallelRequests(
            endpoint, method, requestList, RequestConstants.NONE_TIMEOUT);

    // Verify that first 10 responses are successful and last 3 responses are errors
    httpResponses.subList(0, 10).forEach(response -> Assert.assertTrue(response.isSuccessful()));
    httpResponses.subList(10, 13).forEach(response -> Assert.assertFalse(response.isSuccessful()));
  }

  @Test
  public void testParallelRequestWithTimeout() {
    httpClient =
        new TectonHttpClient(
            this.baseUrlString, this.apiKey, new TectonClientOptions.Builder().build());
    List<String> requestList = prepareRequests(100);
    List<HttpResponse> httpResponses =
        httpClient.performParallelRequests(endpoint, method, requestList, Duration.ofMillis(100));
    // 50 requests with a default maxParallelRequests is not expected to complete in 100 ms
    long numSuccessfulCalls = httpResponses.stream().filter(Objects::nonNull).count();
    Assert.assertTrue(numSuccessfulCalls < 100);
  }

  private List<String> prepareRequests(int size) {
    // Request body will be a string representation of sequential Integer values
    return IntStream.range(0, size).mapToObj(String::valueOf).collect(Collectors.toList());
  }
}
