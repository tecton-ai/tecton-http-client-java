package com.tecton.client.exceptions;

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
