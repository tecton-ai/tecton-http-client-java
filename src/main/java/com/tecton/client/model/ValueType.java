package com.tecton.client.model;

import org.apache.commons.lang3.StringUtils;
import java.util.Arrays;
import java.util.Optional;

public enum ValueType {
  BOOLEAN("boolean"),
  INT64("int64"),
  STRING("string"),
  FLOAT32("float32"),
  FLOAT64("float64"),
  ARRAY("array");

  final String name;

  ValueType(String name) {
    this.name = name;
  }

  String getName() {
    return this.name;
  }

  public static Optional<ValueType> fromString(String name) {
    // Map string to the corresponding ValueType enum
    return Arrays.stream(ValueType.values())
        .filter(val -> StringUtils.equalsIgnoreCase(val.getName(), name))
        .findAny();
  }
}
