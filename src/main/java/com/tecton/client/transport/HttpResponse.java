package com.tecton.client.transport;

import okhttp3.Headers;
import okhttp3.Response;
import java.time.Duration;
import java.util.Optional;

public class HttpResponse {
  private final boolean isSuccessful;
  private final int responseCode;
  private final String message;
  private final String body;
  private final Headers headers;
  private final Duration requestDuration;

  public HttpResponse(Response response) throws Exception {
    this.responseCode = response.code();
    this.headers = response.headers();
    this.requestDuration =
        Duration.ofMillis(response.receivedResponseAtMillis() - response.sentRequestAtMillis());
    this.isSuccessful = response.isSuccessful();
    this.message = response.message();

    if (response.isSuccessful()) {
      this.body = (response.body()).string();
    } else {
      this.body = null;
    }
  }

  public boolean isSuccessful() {
    return isSuccessful;
  }

  public int getResponseCode() {
    return responseCode;
  }

  public Duration getRequestDuration() {
    return requestDuration;
  }

  public String getMessage() {
    return message;
  }

  public Optional<String> getResponseBody() {
    return Optional.ofNullable(this.body);
  }
}
