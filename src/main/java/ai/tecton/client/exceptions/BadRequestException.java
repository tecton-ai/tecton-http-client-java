package ai.tecton.client.exceptions;

public class BadRequestException extends TectonClientException {

  public BadRequestException(String errorMessage, int statusCode) {
    super("Bad Request: " + errorMessage, statusCode);
  }
}
