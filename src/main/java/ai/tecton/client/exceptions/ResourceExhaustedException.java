package ai.tecton.client.exceptions;

public class ResourceExhaustedException extends TectonClientException {

  public ResourceExhaustedException(String errorMessage, int statusCode) {
    super(errorMessage, statusCode);
  }
}
