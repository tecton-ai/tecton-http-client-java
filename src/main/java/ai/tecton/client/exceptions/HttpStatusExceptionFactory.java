package ai.tecton.client.exceptions;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class HttpStatusExceptionFactory {
  private static final Map<Integer, Class<? extends TectonException>> exceptionMappings =
      new HashMap<>();

  static {
    exceptionMappings.put(400, BadRequestException.class);
    exceptionMappings.put(401, UnauthorizedException.class);
    exceptionMappings.put(403, ForbiddenException.class);
    exceptionMappings.put(404, ResourceNotFoundException.class);
    exceptionMappings.put(429, ResourceExhaustedException.class);
    exceptionMappings.put(500, InternalServerErrorException.class);
    exceptionMappings.put(503, ServiceUnavailableException.class);
    exceptionMappings.put(504, GatewayTimeoutExceptionException.class);
  }

  public static Optional<TectonException> createException(int statusCode, String errorMessage) {
    Class<? extends TectonException> exceptionClass = exceptionMappings.get(statusCode);
    if (exceptionClass == null) {
      return Optional.empty();
    }
    try {
      return Optional.of(
          exceptionClass
              .getConstructor(String.class, int.class)
              .newInstance(errorMessage, statusCode));
    } catch (Exception e) {
      throw new RuntimeException("Exception creation failed", e);
    }
  }
}
