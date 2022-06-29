package com.tecton.client.model;

import com.tecton.client.exceptions.TectonClientException;
import com.tecton.client.exceptions.TectonErrorMessage;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

public class FeatureValue {

  private static final SimpleDateFormat dateFormat =
      new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);

  private final String featureNamespace;
  private final String featureName;
  private Instant effectiveTime;
  private final Value value;

  public FeatureValue(
      Object featureObject,
      String name,
      ValueType valueType,
      Optional<ValueType> elementValueType,
      String effectiveTime) {

    String[] split = StringUtils.split(name, ".");
    this.featureNamespace = split[0];
    this.featureName = split[1];

    // Parse effective_time if present
    try {
      if (StringUtils.isNotEmpty(effectiveTime)) {
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        this.effectiveTime = dateFormat.parse(effectiveTime).toInstant();
      }
    } catch (ParseException e) {
      // TODO should we continue if effective_time cannot be parsed?
    }

    // Parse data type from response
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
  }

  public ValueType getValueType() {
    return value.valueType;
  }

  public Optional<ValueType> getListElementType() {
    return Optional.ofNullable(this.value.listValue.listElementType);
  }

  public Instant getEffectiveTime() {
    return effectiveTime;
  }

  public String getFeatureName() {
    return featureName;
  }

  public String getFeatureNamespace() {
    return featureNamespace;
  }

  public class Value {

    private final ValueType valueType;
    private String stringValue;
    private Long int64Value;
    private Boolean booleanValue;
    private Double float64Value;
    private ListDataType listValue;

    // Primitive types
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
          this.int64Value = Long.parseLong((String) featureObject);
          break;
        case FLOAT64:
          this.float64Value = (Double) featureObject;
          break;
        default:
          throw new TectonClientException(
              String.format(TectonErrorMessage.UNKNOWN_DATA_TYPE, valueType.getName()));
      }
    }

    // Array type
    public Value(Object featureObject, ValueType valueType, ValueType listElementType) {
      this.valueType = valueType;
      this.listValue = new ListDataType(listElementType, featureObject);
    }
  }

  public String stringValue() {
    validateValueType(ValueType.STRING);
    return this.value.stringValue;
  }

  public Long int64value() {
    validateValueType(ValueType.INT64);
    return this.value.int64Value;
  }

  public Boolean booleanValue() {
    validateValueType(ValueType.BOOLEAN);
    return this.value.booleanValue;
  }

  public Double float64Value() {
    validateValueType(ValueType.FLOAT64);
    return this.value.float64Value;
  }

  public List<Double> float64ArrayValue() {
    validateValueType(ValueType.ARRAY, ValueType.FLOAT64);
    return this.value.listValue.float64List;
  }

  public List<Float> float32ArrayValue() {
    validateValueType(ValueType.ARRAY, ValueType.FLOAT32);
    return this.value.listValue.float32List;
  }

  public List<Long> int64ArrayValue() {
    validateValueType(ValueType.ARRAY, ValueType.INT64);
    return this.value.listValue.int64List;
  }

  public List<String> stringArrayValue() {
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
}
