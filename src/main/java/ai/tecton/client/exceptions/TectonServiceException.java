package ai.tecton.client.exceptions;

/**
 * Exception class that is used to represent server side errors. This includes Internal Server
 * Errors, Gateway Timeouts etc
 */
public class TectonServiceException extends TectonException {

  public TectonServiceException(String errorMessage) {
    super(errorMessage);
  }

  public TectonServiceException(String errorMessage, int statusCode) {
    super(errorMessage, statusCode);
  }

  public TectonServiceException(String message, Throwable t) {
    super(message, t);
  }

  public TectonServiceException(Throwable t) {
    super(t);
  }
}
