package ai.tecton.client.exceptions;

/**
 * An exception class representing a client error caused by a resource not being found. This
 * exception is typically thrown when the request references a workspace, feature service or other
 * resources that do not exist in Tecton, resulting in a "404 Not Found" HTTP response status. It
 * extends the {@link TectonClientException} class.
 */
public class ResourceNotFoundException extends TectonClientException {

  public ResourceNotFoundException(final String errorMessage, final int statusCode) {

    super("Not Found: " + errorMessage, statusCode);
  }
}
