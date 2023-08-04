package ai.tecton.client.exceptions;

/**
 * An exception class representing a client error caused by forbidden access. This exception is
 * typically thrown when the API Key in the request is authenticated, but the Service Account
 * associated with the API Key is not authorized to access the workspace, resulting in a "403
 * Forbidden" HTTP response status. It extends the {@link TectonClientException} class.
 */
public class ForbiddenException extends TectonClientException {

  public ForbiddenException(String errorMessage, int statusCode) {
    super(errorMessage, statusCode);
  }
}
