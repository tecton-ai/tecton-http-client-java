package ai.tecton.client.exceptions;

/**
 * Exception class that is used to represent an error response received from the FeatureService API.
 * The errorMessage in the exception contains the Tecton error message received in the response
 */
public class TectonServiceException extends RuntimeException {

  public TectonServiceException(String errorMessage) {
    super(errorMessage);
  }

  public TectonServiceException(String message, Throwable t) {
    super(message, t);
  }

  public TectonServiceException(Throwable t) {
    super(t);
  }
}
