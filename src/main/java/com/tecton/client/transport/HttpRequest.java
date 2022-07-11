package com.tecton.client.transport;

import okhttp3.HttpUrl;

class HttpRequest {
  HttpUrl url;
  TectonHttpClient.HttpMethod method;
  String jsonBody;
  String apiKey;

  HttpRequest(
      String baseUrl,
      String endpoint,
      TectonHttpClient.HttpMethod method,
      String apiKey,
      String jsonBody) {
    url = HttpUrl.parse(baseUrl);
    if (endpoint != null && !endpoint.isEmpty()) {
      url = url.newBuilder().addPathSegment(endpoint).build();
    }
    this.method = method;
    this.apiKey = apiKey;
    this.jsonBody = jsonBody;
  }

  HttpUrl getUrl() {
    return this.url;
  }

  TectonHttpClient.HttpMethod getMethod() {
    return method;
  }

  String getApiKey() {
    return apiKey;
  }

  String getJsonBody() {
    return jsonBody;
  }
}
