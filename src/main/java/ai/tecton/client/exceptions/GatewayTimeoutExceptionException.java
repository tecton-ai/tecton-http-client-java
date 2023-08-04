package ai.tecton.client.exceptions;

/**
 * An exception class representing a client error caused by a gateway timeout. This exception is
 * typically thrown when a server acting as a gateway or proxy did not receive a timely response
 * from the Feature Server, resulting in a "504 Gateway Timeout" HTTP response status. It extends
 * the {@link TectonServiceException} class.
 */
public class GatewayTimeoutExceptionException extends TectonServiceException {

  public GatewayTimeoutExceptionException(String errorMessage, int statusCode) {
    super(errorMessage, statusCode);
  }
}
