package com.tecton.client.model;

import com.tecton.client.exceptions.TectonErrorMessage;
import org.apache.commons.lang3.Validate;
import java.util.HashMap;
import java.util.Map;

public class GetFeaturesRequestData {

  private Map<String, String> joinKeyMap;
  private Map<String, Object> requestContextMap;

  public GetFeaturesRequestData() {
    this.joinKeyMap = new HashMap<>();
    this.requestContextMap = new HashMap<>();
  }

  public GetFeaturesRequestData addJoinKey(String key, String value) {
    validateKeyValue(key, value);
    joinKeyMap.put(key, value);
    return this;
  }

  public GetFeaturesRequestData addJoinKey(String key, Long value) {
    validateKeyValue(key, value.toString());
    joinKeyMap.put(key, value.toString());
    return this;
  }

  public GetFeaturesRequestData addRequestContext(String key, String value) {
    validateKeyValue(key, value);
    requestContextMap.put(key, value);
    return this;
  }

  public GetFeaturesRequestData addRequestContext(String key, Long value) {
    validateKeyValue(key, value);
    requestContextMap.put(key, value.toString());
    return this;
  }

  public GetFeaturesRequestData addRequestContext(String key, Double value) {
    validateKeyValue(key, value);
    requestContextMap.put(key, value);
    return this;
  }

  public Map<String, String> getJoinKeyMap() {
    return this.joinKeyMap;
  }

  public Map<String, Object> getRequestContextMap() {
    return requestContextMap;
  }

  public boolean isEmptyJoinKeyMap() {
    return this.joinKeyMap.isEmpty();
  }

  public boolean isEmptyRequestContextMap() {
    return this.requestContextMap.isEmpty();
  }

  private void validateKeyValue(String key, Object value) {
    Validate.notEmpty(key, TectonErrorMessage.INVALID_KEY_VALUE);
    Validate.notNull(value, TectonErrorMessage.INVALID_KEY_VALUE);

    if (value instanceof String) {
      Validate.notEmpty((String) value, TectonErrorMessage.INVALID_KEY_VALUE);
    }
  }
}
