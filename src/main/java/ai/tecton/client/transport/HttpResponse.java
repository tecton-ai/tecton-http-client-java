package ai.tecton.client.transport;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import java.time.Duration;
import java.util.Optional;
import okhttp3.Headers;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class HttpResponse {
  private final boolean isSuccessful;
  private int responseCode;
  private final String message;
  private String body;
  private Headers headers;
  private Duration requestDuration;
  private static final Moshi moshi = new Moshi.Builder().build();
  private static final JsonAdapter<ErrorResponseJson> errorResponseJsonAdapter =
      moshi.adapter(ErrorResponseJson.class);

  HttpResponse(Response response) throws Exception {
    this(response, response.body());
  }

  HttpResponse(Response response, ResponseBody responseBody) throws Exception {
    this.responseCode = response.code();
    this.headers = response.headers();
    this.requestDuration =
        Duration.ofMillis(response.receivedResponseAtMillis() - response.sentRequestAtMillis());
    this.isSuccessful = response.isSuccessful();
    this.body = responseBody.string();

    // If a Tecton error message (e.g. "invalid) 'Tecton-key' authorization header" ) isn't present
    // in the response, (e.g. when the request times out), the client uses the HTTP error status (
    // e.g.Forbidden, Not Found) while throwing an Exception
    if (!this.isSuccessful) {
      this.message = parseErrorResponse(this.body, response.message());
    } else {
      this.message = response.message();
    }
  }

  HttpResponse(String errorMessage) {
    this.isSuccessful = false;
    this.message = errorMessage;
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

  private static String parseErrorResponse(String responseBody, String message) {
    // Parse error response and extract error message
    try {
      ErrorResponseJson errorResponseJson = errorResponseJsonAdapter.fromJson(responseBody);
      return errorResponseJson.message;
    } catch (Exception e) {
      return message;
    }
  }

  static class ErrorResponseJson {
    String error;
    int code;
    String message;
  }
}
