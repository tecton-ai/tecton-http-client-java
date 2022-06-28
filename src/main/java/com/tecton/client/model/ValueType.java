package com.tecton.client.model;

import org.apache.commons.lang3.StringUtils;
import java.util.Arrays;
import java.util.Optional;

enum ValueType {
  BOOLEAN("boolean", Boolean.class),
  INT64("int64", Long.class),
  STRING("string", String.class),
  FLOAT32("float32", Float.class),
  FLOAT64("float64", Double.class),
  ARRAY("array", ListDataType.class);

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

  public static Optional<ValueType> fromString(String name) {
    return Arrays.stream(ValueType.values())
        .filter(val -> StringUtils.equalsIgnoreCase(val.getName(), name))
        .findAny();
  }
}
