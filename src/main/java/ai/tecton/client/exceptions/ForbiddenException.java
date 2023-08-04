package ai.tecton.client.exceptions;

public class ForbiddenException extends TectonClientException {

  public ForbiddenException(String errorMessage, int statusCode) {
    super(errorMessage, statusCode);
  }
}
