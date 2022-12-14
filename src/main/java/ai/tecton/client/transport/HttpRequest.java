package ai.tecton.client.transport;

import okhttp3.HttpUrl;
import org.apache.commons.lang3.StringUtils;

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
      // Paths with leading backslash results in a URL with double backslash.
      // See https://github.com/square/okhttp/issues/2399#issuecomment-195354749
      url = url.newBuilder().addPathSegment(StringUtils.stripStart(endpoint, "/")).build();
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
