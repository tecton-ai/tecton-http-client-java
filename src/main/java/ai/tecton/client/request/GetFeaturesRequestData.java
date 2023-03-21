package ai.tecton.client.request;

import ai.tecton.client.exceptions.TectonClientException;
import ai.tecton.client.exceptions.TectonErrorMessage;
import java.util.HashMap;
import java.util.Map;
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
   * @throws TectonClientException when joinKeyMap is null or empty, or any key or value in the map
   *     is null or empty
   */
  public GetFeaturesRequestData addJoinKeyMap(Map<String, String> joinKeyMap)
      throws TectonClientException {
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
   * @throws TectonClientException when requestContextMap is null or empty, or any key or value in
   *     the map is null or empty
   */
  public GetFeaturesRequestData addRequestContextMap(Map<String, Object> requestContextMap)
      throws TectonClientException {
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
   * @throws TectonClientException when the join key or value is null or empty
   */
  public GetFeaturesRequestData addJoinKey(String key, String value) throws TectonClientException {
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
   * @throws TectonClientException when the join key or value is null or empty
   */
  public GetFeaturesRequestData addJoinKey(String key, Long value) throws TectonClientException {
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
   * @throws TectonClientException when the request context key or value is null or empty
   */
  public GetFeaturesRequestData addRequestContext(String key, String value)
      throws TectonClientException {
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
   * @throws TectonClientException when the request context key or value is null or empty
   */
  public GetFeaturesRequestData addRequestContext(String key, Long value)
      throws TectonClientException {
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
   * @throws TectonClientException when the request context key or value is null or empty
   */
  public GetFeaturesRequestData addRequestContext(String key, Double value)
      throws TectonClientException {
    validateKeyValueDisallowingNullValue(key, value);
    requestContextMap.put(key, value);
    return this;
  }

  Map<String, String> getJoinKeyMap() {
    return this.joinKeyMap;
  }

  Map<String, Object> getRequestContextMap() {
    return requestContextMap;
  }

  boolean isEmptyJoinKeyMap() {
    return this.joinKeyMap.isEmpty();
  }

  boolean isEmptyRequestContextMap() {
    return this.requestContextMap.isEmpty();
  }

  private void validateKeyValue(String key, Object value) {
    Validate.notEmpty(key, TectonErrorMessage.INVALID_KEY_VALUE);
    if (value instanceof String) {
      Validate.notEmpty((String) value, TectonErrorMessage.INVALID_KEY_VALUE);
    }
  }

  private void validateKeyValueDisallowingNullValue(String key, Object value) {
    validateKeyValue(key, value);
    Validate.notNull(value, TectonErrorMessage.INVALID_KEY_VALUE);
  }
}
