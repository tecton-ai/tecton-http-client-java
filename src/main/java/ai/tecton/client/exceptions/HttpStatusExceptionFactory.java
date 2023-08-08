package ai.tecton.client.exceptions;

import java.util.Optional;

/** An exception factory that maps HTTP Status code to a custom Exception */
public class HttpStatusExceptionFactory {

  public static Optional<TectonException> createException(int statusCode, String errorMessage) {
    switch (statusCode) {
      case 400:
        return Optional.of(new BadRequestException(errorMessage, statusCode));
      case 401:
        return Optional.of(new UnauthorizedException(errorMessage, statusCode));
      case 403:
        return Optional.of(new ForbiddenException(errorMessage, statusCode));
      case 404:
        return Optional.of(new ResourceNotFoundException(errorMessage, statusCode));
      case 429:
        return Optional.of(new ResourceExhaustedException(errorMessage, statusCode));
      case 500:
        return Optional.of(new InternalServerErrorException(errorMessage, statusCode));
      case 503:
        return Optional.of(new ServiceUnavailableException(errorMessage, statusCode));
      case 504:
        return Optional.of(new GatewayTimeoutException(errorMessage, statusCode));
      default:
        return Optional.empty();
    }
  }
}
