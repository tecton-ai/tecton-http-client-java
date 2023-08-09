package ai.tecton.client.exceptions;

/**
 * An exception class representing a client error caused by a misconfigured request, such as missing
 * workspace name, missing feature service name etc. It extends the {@link TectonClientException}
 * class.
 */
public class InvalidRequestParameterException extends TectonClientException {

  public InvalidRequestParameterException(final String errorMessage) {
    super(errorMessage);
  }
}
