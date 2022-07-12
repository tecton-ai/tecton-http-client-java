package com.tecton.client.model;

import java.util.Optional;

/** Class that represents the return types for parameters of FeatureServiceMetadata */
public class NameAndType {
  String name;
  ValueType dataType;
  ValueType listElementType;

  /**
   * Constructor that creates a NameAndType with specified name and dataType
   *
   * @param name Name
   * @param dataType one of {@link com.tecton.client.model.ValueType} values
   */
  public NameAndType(String name, ValueType dataType) {
    this.name = name;
    this.dataType = dataType;
  }

  /**
   * Constructor that creates a NameAndType with specified name, dataType and listElementType
   *
   * @param name Name
   * @param dataType dataType
   * @param listElementType array element type when dataType is ARRAY
   */
  public NameAndType(String name, ValueType dataType, ValueType listElementType) {
    this.name = name;
    this.dataType = dataType;
    this.listElementType = listElementType;
  }

  /**
   * Returns the name
   *
   * @return name
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the Tecton data type as a {@link com.tecton.client.model.ValueType}
   *
   * @return {@link com.tecton.client.model.ValueType}
   */
  public ValueType getDataType() {
    return this.dataType;
  }

  /**
   * Returns the array element type if present, Optional.empty() otherwise
   *
   * @return Optional&lt;{@link com.tecton.client.model.ValueType}&gt; if present, Optional.empty()
   *     otherwise
   */
  public Optional<ValueType> getListElementType() {
    return Optional.ofNullable(this.listElementType);
  }
}
