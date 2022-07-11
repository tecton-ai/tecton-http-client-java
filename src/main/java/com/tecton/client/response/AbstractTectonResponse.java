package com.tecton.client.response;

import com.tecton.client.model.ValueType;

import java.time.Duration;
import java.util.Optional;

/** An abstract parent class for Tecton FeatureService API Response subclasses. */
abstract class AbstractTectonResponse {

  private final Duration requestLatency;

  AbstractTectonResponse(Duration requestLatency) {
    this.requestLatency = requestLatency;
  }

  abstract void buildResponseFromJson(String response);

  static class ResponseDataType {
    String type;
    ResponseDataType elementType;

    ValueType getDataType() {
      return ValueType.fromString(type).get();
    }

    Optional<ValueType> getListElementType() {
      if (elementType != null) {
        return ValueType.fromString(elementType.type);
      }
      return Optional.empty();
    }
  }

  /**
   * Returns the response time (network latency + online store latency) as provided by the
   * underlying Http Client
   *
   * @return response time as {@link java.time.Duration}
   */
  public Duration getRequestLatency() {
    return requestLatency;
  }
}
