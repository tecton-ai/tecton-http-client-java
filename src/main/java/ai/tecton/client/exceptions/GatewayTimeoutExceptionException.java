package ai.tecton.client.exceptions;

public class GatewayTimeoutExceptionException extends TectonServiceException {

  public GatewayTimeoutExceptionException(String errorMessage, int statusCode) {
    super(errorMessage, statusCode);
  }
}
