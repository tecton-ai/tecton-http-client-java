package com.tecton.client.response;

import com.tecton.client.model.ValueType;

import java.time.Duration;
import java.util.Optional;

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

  public Duration getRequestLatency() {
    return requestLatency;
  }
}
