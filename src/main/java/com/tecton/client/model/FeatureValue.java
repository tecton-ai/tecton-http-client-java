package com.tecton.client.model;

import com.tecton.client.exceptions.TectonClientException;
import com.tecton.client.exceptions.TectonErrorMessage;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

public class FeatureValue {

  public static final String TYPE = "type";

  private static final SimpleDateFormat dateFormat =
      new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);

  private final String featureNamespace;
  private final String featureName;
  private Instant effectiveTime;
  private final Value value;

  public FeatureValue(
      Object featureObject, String name, Map<String, String> dataType, String effectiveTime) {
    String[] split = StringUtils.split(name, ".");
    featureNamespace = split[0];
    featureName = split[1];

    // TODO Double check date format and zone
    try {
      if (StringUtils.isNotEmpty(effectiveTime)) {
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        this.effectiveTime = dateFormat.parse(effectiveTime).toInstant();
      }
    } catch (ParseException e) {
      // TODO should we continue if effective_time cannot be parsed?
    }

    String type = dataType.get(TYPE);
    Optional<ValueType> valueType = ValueType.fromString(type);
    if (valueType.isPresent()) {
      this.value = new Value(featureObject, valueType.get());
    } else {
      throw new TectonClientException(String.format(TectonErrorMessage.UNKNOWN_DATA_TYPE, type));
    }
  }

  public ValueType getValueType() {
    return value.valueType;
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

  public String getRelativeFeatureName() {
      return StringUtils.join(featureNamespace, ".", featureName);
  }

  public class Value {

    private final ValueType valueType;
    private ValueType listElementType;
    private String stringValue;
    private Long int64Value;
    private Boolean booleanValue;
    private Double float64Value;

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

  public List<Value> listValue() {
      //TODO
    return null;
  }

  private void validateValueType(ValueType valueType) {
    if (this.value.valueType != valueType) {
      throw new TectonClientException(
          String.format(TectonErrorMessage.MISMATCHED_TYPE, value.valueType.getName()));
    }
  }

  enum ValueType {
    BOOLEAN("boolean", Boolean.class),
    INT64("int64", Long.class),
    STRING("string", String.class),
    FLOAT32("float32", Float.class),
    FLOAT64("float64", Double.class),
    ARRAY("array", ArrayList.class);

    String name;
    Class<?> javaClass;

    ValueType(String name, Class<?> javaClass) {
      this.name = name;
      this.javaClass = javaClass;
    }

    String getName() {
      return this.name;
    }

    Class<?> getJavaClass() {
      return this.javaClass;
    }

    static Optional<ValueType> fromString(String name) {
      return Arrays.stream(ValueType.values())
          .filter(val -> StringUtils.equalsIgnoreCase(val.getName(), name))
          .findAny();
    }
  }
}
