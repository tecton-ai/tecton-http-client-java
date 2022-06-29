package com.tecton.client.model;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FeatureServiceMetadata {

  List<NameAndType> inputJoinKeys;
  List<NameAndType> inputRequestContextKeys;
  List<NameAndType> featureValues;

  public FeatureServiceMetadata(
      List<NameAndType> inputJoinKeys,
      List<NameAndType> inputRequestContextKeys,
      List<NameAndType> featureValues) {
    this.inputJoinKeys = inputJoinKeys;
    this.inputRequestContextKeys = inputRequestContextKeys;
    this.featureValues = featureValues;
  }

  public List<NameAndType> getInputJoinKeys() {
    return this.inputJoinKeys;
  }

  public List<NameAndType> getInputRequestContextKeys() {
    return inputRequestContextKeys;
  }

  public List<NameAndType> getFeatureValues() {
    return featureValues;
  }

  public Map<String, NameAndType> getInputJoinKeysAsMap() {
    return inputJoinKeys.stream()
        .collect(Collectors.toMap(NameAndType::getName, Function.identity()));
  }

  public Map<String, NameAndType> getInputRequestContextKeysAsMap() {
    return inputRequestContextKeys.stream()
        .collect(Collectors.toMap(NameAndType::getName, Function.identity()));
  }

  public Map<String, NameAndType> getFeatureValuesAsMap() {
    return featureValues.stream()
        .collect(Collectors.toMap(NameAndType::getName, Function.identity()));
  }
}
