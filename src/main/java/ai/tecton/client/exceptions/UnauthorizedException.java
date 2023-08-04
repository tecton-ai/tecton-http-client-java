package ai.tecton.client.exceptions;

/**
 * An exception class representing a client error caused by unauthorized access. This exception is
 * typically thrown when the API Key in the request is missing or invalid, resulting in a "401
 * Unauthorized" HTTP response status. It extends the {@link TectonClientException} class.
 */
public class UnauthorizedException extends TectonClientException {

  public UnauthorizedException(String errorMessage, int statusCode) {

    super("Unauthorized: " + errorMessage, statusCode);
  }
}
