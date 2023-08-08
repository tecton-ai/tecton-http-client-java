package ai.tecton.client.exceptions;

/**
 * An exception class representing a server error caused by a temporarily unavailable service. This
 * exception is typically thrown when the Tecton is temporarily unable to handle the client's
 * request due to being overloaded or undergoing maintenance, resulting in a "503 Service
 * Unavailable" HTTP response status. It extends the {@link TectonServiceException} class.
 */
public class ServiceUnavailableException extends TectonServiceException {

  public ServiceUnavailableException(final String errorMessage, final int statusCode) {
    super(errorMessage, statusCode);
  }
}
