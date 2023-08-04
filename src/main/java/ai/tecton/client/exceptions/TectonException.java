package ai.tecton.client.exceptions;

import java.util.Optional;

/**
 * Exception class that is used to represent various errors encountered by the client. It includes a
 * {@link TectonErrorMessage} that provides more information about the cause of the exception
 */
public class TectonException extends RuntimeException {
  int statusCode;

  public TectonException(String errorMessage) {
    super(errorMessage);
  }

  public TectonException(String errorMessage, int statusCode) {
    super(errorMessage);
    this.statusCode = statusCode;
  }

  public TectonException(String message, Throwable t) {
    super(message, t);
  }

  public TectonException(Throwable t) {
    super(t);
  }

  public Optional<Integer> getStatusCode() {
    return Optional.of(statusCode);
  }
}
