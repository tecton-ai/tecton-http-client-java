package ai.tecton.client.transport;

import ai.tecton.client.TectonClientOptions;
import java.time.Duration;
import okhttp3.Request;
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

  @Before
  public void setup() {
    url = "http://test-url.com";
    endpoint = "/api/v1/feature-service/get-features";
    apiKey = "12345";
    body = "{}";
    httpClient = new TectonHttpClient(url, apiKey, new TectonClientOptions.Builder().build());
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
}
