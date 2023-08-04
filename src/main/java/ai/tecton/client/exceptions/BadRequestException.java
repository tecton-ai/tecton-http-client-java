package ai.tecton.client.exceptions;

/**
 * An exception class representing a client error caused by a bad request. This exception is
 * typically thrown when the client's request is malformed or contains invalid data, resulting in a
 * "400 Bad Request" HTTP response status. It extends the {@link TectonClientException} class.
 */
public class BadRequestException extends TectonClientException {

  public BadRequestException(String errorMessage, int statusCode) {
    super("Bad Request: " + errorMessage, statusCode);
  }
}
