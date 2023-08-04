package ai.tecton.client.exceptions;

public class InternalServerErrorException extends TectonServiceException {

  public InternalServerErrorException(String errorMessage, int statusCode) {
    super(errorMessage, statusCode);
  }
}
