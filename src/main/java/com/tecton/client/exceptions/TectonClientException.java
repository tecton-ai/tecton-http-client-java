package com.tecton.client.exceptions;

/**
 * Exception class that is used to represent various errors encountered by the client. Each
 * TectonClientException includes an error message that provides information about the cause of the
 * exception
 */
public class TectonClientException extends RuntimeException {

  public TectonClientException(String errorMessage) {
    super(errorMessage);
  }

  public TectonClientException(String message, Throwable t) {
    super(message, t);
  }

  public TectonClientException(Throwable t) {
    super(t);
  }
}
