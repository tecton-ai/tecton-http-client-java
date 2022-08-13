package ai.tecton.client.transport;

import ai.tecton.client.exceptions.TectonClientException;
import ai.tecton.client.exceptions.TectonErrorMessage;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import java.time.Duration;
import java.util.Optional;
import okhttp3.Headers;
import okhttp3.Response;

public class HttpResponse {
  private final boolean isSuccessful;
  private final int responseCode;
  private final String message;
  private final String body;
  private final Headers headers;
  private final Duration requestDuration;
  private static final Moshi moshi = new Moshi.Builder().build();
  private static final JsonAdapter<TectonHttpClient.ErrorResponseJson> errorResponseJsonAdapter =
      moshi.adapter(TectonHttpClient.ErrorResponseJson.class);

  HttpResponse(Response response) throws Exception {
    this.responseCode = response.code();
    this.headers = response.headers();
    this.requestDuration =
        Duration.ofMillis(response.receivedResponseAtMillis() - response.sentRequestAtMillis());
    this.isSuccessful = response.isSuccessful();
    this.body = response.body().string();
    if (!this.isSuccessful) {
      this.message = parseErrorResponse(this.body);
    } else {
      this.message = response.message();
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

  private static String parseErrorResponse(String responseBody) {
    // Parse error response and extract error message
    try {
      TectonHttpClient.ErrorResponseJson errorResponseJson =
          errorResponseJsonAdapter.fromJson(responseBody);
      return errorResponseJson.message;
    } catch (Exception e) {
      throw new TectonClientException(TectonErrorMessage.INVALID_RESPONSE_FORMAT);
    }
  }
}
