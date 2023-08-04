package ai.tecton.client.exceptions;

/**
 * Exception class that is used to represent various errors encountered by the client. It includes a
 * {@link TectonErrorMessage} that provides more information about the cause of the exception
 */
public class TectonClientException extends TectonException {

  public TectonClientException(String errorMessage) {
    super(errorMessage);
  }

  public TectonClientException(String errorMessage, int statusCode) {
    super(errorMessage, statusCode);
  }

  public TectonClientException(String message, Throwable t) {
    super(message, t);
  }

  public TectonClientException(Throwable t) {
    super(t);
  }
}
