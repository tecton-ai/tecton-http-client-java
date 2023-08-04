package ai.tecton.client.exceptions;

public class UnauthorizedException extends TectonClientException {

  public UnauthorizedException(String errorMessage, int statusCode) {

    super("Unauthorized: " + errorMessage, statusCode);
  }
}
