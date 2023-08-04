package ai.tecton.client.exceptions;

/**
 * An exception class representing a server error caused by an internal server failure. This
 * exception is typically thrown when an unexpected error occurs on the server, resulting in a "500
 * Internal Server Error" HTTP response status. It extends the {@link TectonServiceException} class.
 */
public class InternalServerErrorException extends TectonServiceException {

  public InternalServerErrorException(String errorMessage, int statusCode) {
    super(errorMessage, statusCode);
  }
}
