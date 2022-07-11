package com.tecton.client.exceptions;

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
