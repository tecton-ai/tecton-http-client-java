package ai.tecton.client.model;

import ai.tecton.client.exceptions.TectonClientException;
import ai.tecton.client.exceptions.TectonErrorMessage;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

/**
 * Class that represents each feature value in the feature vector returned in the
 * GetFeaturesResponse
 */
public class FeatureValue {

  private final String featureNamespace;
  private final String featureName;
  private Instant effectiveTime;
  private final Value value;
  private final Optional<FeatureStatus> featureStatus;
  private final String featureDescription;
  private final Map<String, String> featureTags;

  public FeatureValue(
      Object featureObject,
      String name,
      ValueType valueType,
      Optional<ValueType> elementValueType,
      String effectiveTime,
      Optional<FeatureStatus> featureStatus,
      String featureDescription,
      Map<String, String> featureTags) {

    // Split name into feature namespace and feature name
    String[] split = StringUtils.split(name, ".");
    featureNamespace = split[0];
    featureName = split[1];
    this.featureStatus = featureStatus;
    this.featureDescription = featureDescription;
    this.featureTags = featureTags;

    // Parse effective_time if present
    try {
      if (StringUtils.isNotEmpty(effectiveTime)) {
        this.effectiveTime = OffsetDateTime.parse(effectiveTime).toInstant();
      }
    } catch (Exception e) {
      throw new TectonClientException(TectonErrorMessage.UNKNOWN_DATETIME_FORMAT);
    }

    try {
      // Create Value using valueType
      switch (valueType) {
        case ARRAY:
          this.value = new Value(featureObject, valueType, elementValueType.get());
          break;
        case STRING:
        case INT64:
        case BOOLEAN:
        case FLOAT32:
        case FLOAT64:
        default:
          this.value = new Value(featureObject, valueType);
      }
    } catch (Exception e) {
      throw new TectonClientException(
          String.format(
              TectonErrorMessage.INVALID_DATA_TYPE, name, valueType.getName(), featureObject));
    }
  }

  /**
   * Returns the ValueType representing the Tecton data_type for the feature value. Currently
   * supported types are ARRAY, STRING, INT64, BOOLEAN and FLOAT64
   *
   * @return {@link ValueType} of the feature value
   */
  public ValueType getValueType() {
    return value.valueType;
  }

  /**
   * Returns the individual array element type if the feature valueType is ARRAY.
   *
   * @return {@link ValueType} of the feature values in the array wrapped in {@link
   *     java.util.Optional} if the getValueType() is ARRAY, Optional.empty() otherwise
   */
  public Optional<ValueType> getListElementType() {
    return Optional.ofNullable(this.value.listValue.listElementType);
  }

  /**
   * Returns the feature status of the feature value. PRESENT if the feature value is retrieved and
   * present in the online store or MISSING_DATA if the feature value is missing or outside TTL
   *
   * @return Optional&lt;String&gt; of the feature value statuses {@link java.util.Optional}.
   */
  public Optional<FeatureStatus> getFeatureStatus() {
    return this.featureStatus;
  }

  /**
   * Returns the description of the feature
   *
   * @return String
   */
  public String getFeatureDescription() {
    return this.featureDescription;
  }

  /** Returns the tags of the feature */
  public Map<String, String> getFeatureTags() {
    return this.featureTags;
  }

  /**
   * Returns the effective serving time for this feature. This is the most recent time that's
   * aligned to the interval for which a full aggregation is available for this feature. Note: Only
   * present if MetadataOption.EFFECTIVE_TIME is included in the GetFeaturesRequest
   *
   * @return Optional&lt;Instant&gt; representing the effectiveTime if present, Optional.empty()
   *     otherwise
   */
  public Optional<Instant> getEffectiveTime() {
    return Optional.ofNullable(effectiveTime);
  }

  /** Returns the feature service name */
  public String getFeatureName() {
    return featureName;
  }

  /** Returns the feature service namespace */
  public String getFeatureNamespace() {
    return featureNamespace;
  }

  class Value {

    private final ValueType valueType;
    private String stringValue;
    private Long int64Value;
    private Boolean booleanValue;
    private Double float64Value;
    private ListDataType listValue;

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Value value = (Value) o;
      return valueType == value.valueType
          && Objects.equals(stringValue, value.stringValue)
          && Objects.equals(int64Value, value.int64Value)
          && Objects.equals(booleanValue, value.booleanValue)
          && Objects.equals(float64Value, value.float64Value)
          && Objects.equals(listValue, value.listValue);
    }

    @Override
    public int hashCode() {
      return Objects.hash(
          valueType, stringValue, int64Value, booleanValue, float64Value, listValue);
    }

    // Primitive types
    // Float32 is currently not a supported type for feature values in Tecton. Refer here for all
    // supported types :
    // https://docs.tecton.ai/docs/faqs/creating_and_managing_features/#what-data-types-are-supported-for-feature-values
    public Value(Object featureObject, ValueType valueType) {
      this.valueType = valueType;
      switch (valueType) {
        case BOOLEAN:
          this.booleanValue = (Boolean) featureObject;
          break;
        case STRING:
          this.stringValue = (String) featureObject;
          break;
        case INT64:
          // Tecton represents all Int64 feature values as JSON strings in the response.
          String stringValue = (String) featureObject;
          if (stringValue != null) {
            this.int64Value = Long.parseLong(stringValue);
          }
          break;
        case FLOAT64:
          // Tecton also represents all double feature values as JSON numbers in the response.
          if (featureObject instanceof String) {
            String doubleString = (String) featureObject;
            if (doubleString.equals("null")
                || doubleString.equals("NaN")
                || doubleString.equals("Infinity")
                || doubleString.equals("-Infinity")) {
              this.float64Value = null;
            } else {
              this.float64Value = Double.valueOf(doubleString);
            }
          } else {
            this.float64Value = (Double) featureObject;
          }
          break;
        default:
          throw new TectonClientException(
              String.format(TectonErrorMessage.UNKNOWN_DATA_TYPE, valueType.getName()));
      }
    }

    // Array type
    Value(Object featureObject, ValueType valueType, ValueType listElementType) {
      this.valueType = valueType;
      this.listValue = new ListDataType(listElementType, featureObject);
    }
  }

  /**
   * A Feature Value of type String
   *
   * @return feature value cast to java.lang.String
   * @throws TectonClientException if the method is called on a value whose ValueType is not STRING
   */
  public String stringValue() throws TectonClientException {
    validateValueType(ValueType.STRING);
    return this.value.stringValue;
  }

  /**
   * A Feature Value of type int64 (Long)
   *
   * @return feature value cast to java.lang.Long
   * @throws TectonClientException if the method is called on a value whose ValueType is not INT64
   */
  public Long int64value() throws TectonClientException {
    validateValueType(ValueType.INT64);
    return this.value.int64Value;
  }

  /**
   * A Feature Value of type Boolean
   *
   * @return feature value cast to java.lang.Boolean
   * @throws TectonClientException if the method is called on a value whose ValueType is not BOOLEAN
   */
  public Boolean booleanValue() throws TectonClientException {
    validateValueType(ValueType.BOOLEAN);
    return this.value.booleanValue;
  }

  /**
   * A Feature Value of type Float64 (Double)
   *
   * @return feature value cast to java.lang.Double
   * @throws TectonClientException if the method is called on a value whose ValueType is not FLOAT64
   */
  public Double float64Value() throws TectonClientException {
    validateValueType(ValueType.FLOAT64);
    return this.value.float64Value;
  }

  /**
   * A Feature Value of type ARRAY with FLOAT64 values
   *
   * @return feature value cast to List&lt;Double&gt;
   * @throws TectonClientException if the method is called on a value whose valueType is not ARRAY
   *     or listElementType is not FLOAT64
   */
  public List<Double> float64ArrayValue() throws TectonClientException {
    validateValueType(ValueType.ARRAY, ValueType.FLOAT64);
    return this.value.listValue.float64List;
  }

  /**
   * A Feature Value of type ARRAY with FLOAT32 values
   *
   * @return feature value cast to List&lt;Float&gt;
   * @throws TectonClientException if the method is called on a value whose valueType is not ARRAY
   *     or listElementType is not FLOAT32
   */
  public List<Float> float32ArrayValue() throws TectonClientException {
    validateValueType(ValueType.ARRAY, ValueType.FLOAT32);
    return this.value.listValue.float32List;
  }

  /**
   * A Feature Value of type ARRAY with INT64 values
   *
   * @return feature value cast to List&lt;Long&gt;
   * @throws TectonClientException if the method is called on a value whose valueType is not ARRAY
   *     or listElementType is not INT64
   */
  public List<Long> int64ArrayValue() throws TectonClientException {
    validateValueType(ValueType.ARRAY, ValueType.INT64);
    return this.value.listValue.int64List;
  }

  /**
   * A Feature Value of type ARRAY with String values
   *
   * @return feature value cast to List&lt;String&gt;
   * @throws TectonClientException if the method is called on a value whose valueType is not ARRAY
   *     or listElementType is not STRING
   */
  public List<String> stringArrayValue() throws TectonClientException {
    validateValueType(ValueType.ARRAY, ValueType.STRING);
    return this.value.listValue.stringList;
  }

  private void validateValueType(ValueType valueType) {
    if (this.value.valueType != valueType) {
      throw new TectonClientException(
          String.format(TectonErrorMessage.MISMATCHED_TYPE, value.valueType.getName()));
    }
  }

  private void validateValueType(ValueType valueType, ValueType elementType) {
    validateValueType(valueType);
    if (this.value.listValue.listElementType != elementType) {
      throw new TectonClientException(
          String.format(
              TectonErrorMessage.MISMATCHED_TYPE, value.listValue.listElementType.getName()));
    }
  }

  /** Overrides <i>equals()</i> in class {@link Object} */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    FeatureValue that = (FeatureValue) o;
    return Objects.equals(featureNamespace, that.featureNamespace)
        && Objects.equals(featureName, that.featureName)
        && Objects.equals(effectiveTime, that.effectiveTime)
        && Objects.equals(value, that.value)
        && Objects.equals(featureStatus, that.featureStatus);
  }

  /** Overrides <i>hashCode()</i> in class {@link Object} */
  @Override
  public int hashCode() {
    return Objects.hash(featureNamespace, featureName, effectiveTime, value, featureStatus);
  }
}
