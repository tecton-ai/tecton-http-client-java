package com.tecton.client.transport;

import okhttp3.HttpUrl;

public class HttpRequest {
  HttpUrl url;
  TectonHttpClient.HttpMethod method;
  String jsonBody;
  String apiKey;

  public HttpRequest(
      String baseUrl,
      String endpoint,
      TectonHttpClient.HttpMethod method,
      String apiKey,
      String jsonBody) {
      url = HttpUrl.parse(baseUrl);
      if(endpoint!=null && !endpoint.isEmpty()) {
          url = url.newBuilder().addPathSegment(endpoint).build();
      }
    this.method = method;
    this.apiKey = apiKey;
    this.jsonBody = jsonBody;
  }

  public HttpUrl getUrl() {
    return this.url;
  }

  public TectonHttpClient.HttpMethod getMethod() {
    return method;
  }

  public String getApiKey() {
    return apiKey;
  }

  public String getJsonBody() {
    return jsonBody;
  }
}
