package com.tecton.client;

import com.tecton.client.transport.HttpRequest;
import com.tecton.client.transport.HttpResponse;
import com.tecton.client.transport.TectonHttpClient;
import com.tecton.client.transport.TectonHttpClient.HttpMethod;
import com.tecton.client.transport.TectonHttpClient.HttpHeader;
import com.tecton.client.transport.TectonHttpClient.MediaType;
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
    url = "http://staging.tecton.ai";
    endpoint = "/api/v1/feature-service/get-features";
    apiKey = "f4e27a3a1139cc4b1b1b72d59318557f";
    body = "{}";
    httpClient = new TectonHttpClient();
  }

  @Test
  public void testDefaultHttpClient() {
      TectonHttpClient httpClient = new TectonHttpClient();
      Assert.assertEquals(5, httpClient.getConnectTimeout().getSeconds());
      Assert.assertEquals(5, httpClient.getReadTimeout().getSeconds());
      Assert.assertFalse(httpClient.isClosed());
  }

  @Test
  public void testDefaultTectonRequest() {
    HttpRequest httpRequest = new HttpRequest(url, endpoint, HttpMethod.POST, apiKey, body);

    Request request = httpClient.buildRequestWithDefaultHeaders(httpRequest);
    Assert.assertNotNull(request);

    Assert.assertEquals(POST, request.method());
    Assert.assertEquals(
        request.headers().get(HttpHeader.CONTENT_TYPE.getName()),
        MediaType.APPLICATION_JSON.getName());
    Assert.assertEquals(MediaType.APPLICATION_JSON.getName(),
        request.headers().get(HttpHeader.ACCEPT.getName()));
    Assert.assertEquals("Tecton-key " + apiKey, request.headers().get(HttpHeader.AUTHORIZATION.getName()));

      HttpResponse response = httpClient.performRequest(httpRequest);
      Assert.assertNotNull(response);
  }
}