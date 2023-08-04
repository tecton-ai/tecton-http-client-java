package ai.tecton.client.exceptions;

import java.util.Optional;

/** Base class that is used to represent various errors encountered by the Java client. */
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

  /**
   * Returns the HTTP Status Code associated with the exception
   *
   * @return HTTP Status Code if present, Optional.empty() otherwise
   */
  public Optional<Integer> getStatusCode() {
    return Optional.of(statusCode);
  }
}
