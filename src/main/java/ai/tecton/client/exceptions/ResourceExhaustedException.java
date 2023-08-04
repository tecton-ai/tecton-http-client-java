package ai.tecton.client.exceptions;

/**
 * An exception class representing a client error caused by resource exhaustion. This exception
 * indicates that request rate exceeds the concurrent request limit set for your deployment,
 * resulting in a "429 Too Many Requests" HTTP response status. It extends the {@link
 * TectonClientException} class.
 */
public class ResourceExhaustedException extends TectonClientException {

  public ResourceExhaustedException(String errorMessage, int statusCode) {
    super(errorMessage, statusCode);
  }
}
