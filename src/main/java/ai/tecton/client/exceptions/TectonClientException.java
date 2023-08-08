package ai.tecton.client.exceptions;

/**
 * Exception class that is used to represent various client side errors, such as bad request
 * parameters, unauthorized requests etc.
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
