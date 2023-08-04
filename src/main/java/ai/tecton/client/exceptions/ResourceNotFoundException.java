package ai.tecton.client.exceptions;

public class ResourceNotFoundException extends TectonClientException {

  public ResourceNotFoundException(String errorMessage, int statusCode) {

    super("Not Found: " + errorMessage, statusCode);
  }
}
