package com.tecton.client.model;

import java.util.Optional;

public class NameAndType {
  String name;
  ValueType dataType;
  ValueType listElementType;

  public NameAndType(String name, ValueType dataType) {
    this.name = name;
    this.dataType = dataType;
  }

  public NameAndType(String name, ValueType dataType, ValueType listElementType) {
    this.name = name;
    this.dataType = dataType;
    this.listElementType = listElementType;
  }

  public String getName() {
    return name;
  }

  public ValueType getDataType() {
    return this.dataType;
  }

  public Optional<ValueType> getListElementType() {
    return Optional.ofNullable(this.listElementType);
  }
}
