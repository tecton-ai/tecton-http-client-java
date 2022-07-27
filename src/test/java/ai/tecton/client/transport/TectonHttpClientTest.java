package ai.tecton.client.transport;

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
    httpClient = new TectonHttpClient(url, apiKey);
  }

  @Test
  public void testDefaultHttpClient() {
    TectonHttpClient httpClient = new TectonHttpClient(url, apiKey);
    Assert.assertEquals(5, httpClient.getConnectTimeout().getSeconds());
    Assert.assertEquals(5, httpClient.getReadTimeout().getSeconds());
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
}
