package ai.tecton.client.request;

import ai.tecton.client.exceptions.InvalidRequestParameterException;
import ai.tecton.client.exceptions.TectonErrorMessage;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.Validate;

/** Class that represents the map parameters passed to a {@link GetFeaturesRequest} */
public class GetFeaturesRequestData {

  private Map<String, String> joinKeyMap;
  private Map<String, Object> requestContextMap;

  /**
   * Constructor that creates a new GetFeaturesRequestData object with an empty joinKeyMap and
   * requestContextMap
   */
  public GetFeaturesRequestData() {
    this.joinKeyMap = new HashMap<>();
    this.requestContextMap = new HashMap<>();
  }

  /**
   * Setter method for joinKeyMap
   *
   * @param joinKeyMap Join keys used for table-based FeatureViews.
   *     <p>The key of this map is the join key name and the value is the join key value for this
   *     request
   *     <p>For string keys, the value should be a string
   *     <p>For int64 (Long) keys, the value should be a string of the decimal representation of the
   *     integer
   * @return Returns the GetFeaturesRequestData object after setting joinKeyMap
   * @throws InvalidRequestParameterException when joinKeyMap is null or empty, or any key or value
   *     in the map is null or empty
   */
  public GetFeaturesRequestData addJoinKeyMap(Map<String, String> joinKeyMap)
      throws InvalidRequestParameterException {
    Validate.notEmpty(joinKeyMap);
    joinKeyMap.forEach(this::validateKeyValue);
    this.joinKeyMap = joinKeyMap;
    return this;
  }

  /**
   * Setter method for requestContextMap
   *
   * @param requestContextMap Request context used for OnDemand FeatureViews.
   *     <p>The key of this map is the join key name and the value is the join key value for this
   *     request
   *     <p>For string values, the value should be a java.lang.String
   *     <p>For int64 values, the value should be a java.lang.String of the decimal representation
   *     of the integer
   *     <p>For double values, the value should be a java.lang.Double
   * @return Returns the GetFeaturesRequestData object after setting requestContextMap
   * @throws InvalidRequestParameterException when requestContextMap is null or empty, or any key or
   *     value in the map is null or empty
   */
  public GetFeaturesRequestData addRequestContextMap(Map<String, Object> requestContextMap)
      throws InvalidRequestParameterException {
    Validate.notEmpty(requestContextMap);
    requestContextMap.forEach(this::validateKeyValueDisallowingNullValue);
    this.requestContextMap = requestContextMap;
    return this;
  }

  /**
   * Add a String join key value to the joinKeyMap
   *
   * @param key join key name
   * @param value String join value
   * @return Returns the GetFeaturesRequestData object after adding the join key value
   * @throws InvalidRequestParameterException when the join key or value is null or empty
   */
  public GetFeaturesRequestData addJoinKey(String key, String value)
      throws InvalidRequestParameterException {
    validateKeyValue(key, value);
    joinKeyMap.put(key, value);
    return this;
  }

  /**
   * Add an int64 join key value to the joinKeyMap
   *
   * @param key join key name
   * @param value int64 (Long) join value
   * @return Returns the GetFeaturesRequestData object after adding the join key value
   * @throws InvalidRequestParameterException when the join key or value is null or empty
   */
  public GetFeaturesRequestData addJoinKey(String key, Long value)
      throws InvalidRequestParameterException {
    String joinKeyValue = (value == null) ? null : value.toString();
    validateKeyValue(key, joinKeyValue);
    joinKeyMap.put(key, joinKeyValue);
    return this;
  }

  /**
   * Add a String request context value to the requestContextMap
   *
   * @param key request context name
   * @param value String request context value
   * @return Returns the GetFeaturesRequestData object after adding the request context key value
   * @throws InvalidRequestParameterException when the request context key or value is null or empty
   */
  public GetFeaturesRequestData addRequestContext(String key, String value)
      throws InvalidRequestParameterException {
    validateKeyValueDisallowingNullValue(key, value);
    requestContextMap.put(key, value);
    return this;
  }

  /**
   * Add an int64 request context value to the requestContextMap
   *
   * @param key request context name
   * @param value int64 (Long) request context value
   *     <p>Note: The int64 value is converted to a String of the decimal representation of the
   *     integer
   * @return Returns the GetFeaturesRequestData object after adding the request context key value
   * @throws InvalidRequestParameterException when the request context key or value is null or empty
   */
  public GetFeaturesRequestData addRequestContext(String key, Long value)
      throws InvalidRequestParameterException {
    validateKeyValueDisallowingNullValue(key, value);
    requestContextMap.put(key, value.toString());
    return this;
  }

  /**
   * Add a Double request context value to the requestContextMap
   *
   * @param key request context name
   * @param value Double request context value
   * @return Returns the GetFeaturesRequestData object after adding the request context key value
   * @throws InvalidRequestParameterException when the request context key or value is null or empty
   */
  public GetFeaturesRequestData addRequestContext(String key, Double value)
      throws InvalidRequestParameterException {
    validateKeyValueDisallowingNullValue(key, value);
    requestContextMap.put(key, value);
    return this;
  }

  public Map<String, String> getJoinKeyMap() {
    return Collections.unmodifiableMap(this.joinKeyMap);
  }

  public Map<String, Object> getRequestContextMap() {
    return Collections.unmodifiableMap(this.requestContextMap);
  }

  public boolean isEmptyJoinKeyMap() {
    return this.joinKeyMap.isEmpty();
  }

  public boolean isEmptyRequestContextMap() {
    return this.requestContextMap.isEmpty();
  }

  private void validateKeyValue(String key, Object value) {
    try {
      Validate.notEmpty(key, TectonErrorMessage.INVALID_KEY_VALUE);
      if (value instanceof String) {
        Validate.notEmpty((String) value, TectonErrorMessage.INVALID_KEY_VALUE);
      }
    } catch (Exception e) {
      throw new InvalidRequestParameterException(e.getMessage());
    }
  }

  private void validateKeyValueDisallowingNullValue(String key, Object value) {
    try {
      validateKeyValue(key, value);
      Validate.notNull(value, TectonErrorMessage.INVALID_KEY_VALUE);
    } catch (Exception e) {
      throw new InvalidRequestParameterException(e.getMessage());
    }
  }

  /** A Builder class for creating an instance of {@link GetFeaturesRequestData} object */
  public static class Builder {
    private GetFeaturesRequestData getFeaturesRequestData;

    /** Instantiates a new Builder */
    public Builder() {
      getFeaturesRequestData = new GetFeaturesRequestData();
    }

    /**
     * Setter for joinKeyMap
     *
     * @param joinKeyMap Join keys used for table-based FeatureViews.
     *     <p>The key of this map is the join key name and the value is the join key value for this
     *     request
     *     <p>For string keys, the value should be a string
     *     <p>For int64 (Long) keys, the value should be a string of the decimal representation of
     *     the integer
     * @return this Builder
     */
    public Builder joinKeyMap(Map<String, String> joinKeyMap) {
      getFeaturesRequestData = getFeaturesRequestData.addJoinKeyMap(joinKeyMap);
      return this;
    }

    /**
     * Setter for requestContextMap
     *
     * @param requestContextMap Request context used for OnDemand FeatureViews.
     *     <p>The key of this map is the join key name and the value is the join key value for this
     *     request
     *     <p>For string values, the value should be a java.lang.String
     *     <p>For int64 values, the value should be a java.lang.String of the decimal representation
     *     of the integer
     *     <p>For double values, the value should be a java.lang.Double
     * @return this Builder
     */
    public Builder requestContextMap(Map<String, Object> requestContextMap) {
      getFeaturesRequestData = getFeaturesRequestData.addRequestContextMap(requestContextMap);
      return this;
    }

    /**
     * Build a {@link GetFeaturesRequestData} object from the Builder
     *
     * @return {@link GetFeaturesRequestData}
     */
    public GetFeaturesRequestData build() {
      return this.getFeaturesRequestData;
    }
  }

  /** Overrides <i>equals()</i> in class {@link Object} */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    GetFeaturesRequestData that = (GetFeaturesRequestData) o;
    return Objects.equals(joinKeyMap, that.joinKeyMap)
        && Objects.equals(requestContextMap, that.requestContextMap);
  }

  /** Overrides <i>hashCode()</i> in class {@link Object} */
  @Override
  public int hashCode() {
    return Objects.hash(joinKeyMap, requestContextMap);
  }
}
