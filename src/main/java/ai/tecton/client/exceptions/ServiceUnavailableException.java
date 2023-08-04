package ai.tecton.client.exceptions;

public class ServiceUnavailableException extends TectonServiceException {

  public ServiceUnavailableException(String errorMessage, int statusCode) {
    super(errorMessage, statusCode);
  }
}
