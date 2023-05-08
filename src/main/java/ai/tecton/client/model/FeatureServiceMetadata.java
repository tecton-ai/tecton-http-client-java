package ai.tecton.client.model;

import ai.tecton.client.request.GetFeaturesRequest;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Class that encapsulates metadata for a FeatureService, including the schema for join keys and
 * request context in GetFeaturesRequestData, as well as the in-order output schema of feature
 * values in GetFeaturesResponse.
 */
public class FeatureServiceMetadata {

  private final List<NameAndType> inputJoinKeys;
  private final List<NameAndType> inputRequestContextKeys;
  private final List<NameAndType> featureValues;

  public FeatureServiceMetadata(
      List<NameAndType> inputJoinKeys,
      List<NameAndType> inputRequestContextKeys,
      List<NameAndType> featureValues) {
    this.inputJoinKeys = inputJoinKeys;
    this.inputRequestContextKeys = inputRequestContextKeys;
    this.featureValues = featureValues;
  }

  /**
   * Get the input join keys that are expected to be passed in the joinKeyMap parameter for a {@link
   * GetFeaturesRequest} for the feature service
   *
   * @return Returns a {@link java.util.List} of {@link NameAndType} representing the input join
   *     keys
   */
  public List<NameAndType> getInputJoinKeys() {
    return this.inputJoinKeys;
  }

  /**
   * Get the request context keys that are expected to be passed in the requestContextMap parameter
   * for a {@link GetFeaturesRequest} for the feature service
   *
   * @return Returns a {@link java.util.List} of {@link NameAndType} representing the input request
   *     context keys
   */
  public List<NameAndType> getInputRequestContextKeys() {
    return this.inputRequestContextKeys;
  }

  /**
   * Get metadata for feature values to be returned in the GetFeaturesResponse.
   *
   * <p>The order of returned features will match the order returned by GetFeaturesResponse
   *
   * @return Returns a {@link java.util.List} of {@link NameAndType} representing the feature
   *     metadata
   */
  public List<NameAndType> getFeatureValues() {
    return this.featureValues;
  }

  /** Returns the input join keys as a {@link java.util.Map} with the join key name as the key */
  public Map<String, NameAndType> getInputJoinKeysAsMap() {
    return this.inputJoinKeys.stream()
        .collect(Collectors.toMap(NameAndType::getName, Function.identity()));
  }

  /**
   * Returns the request context keys as a {@link java.util.Map} with the request context name as
   * the key
   */
  public Map<String, NameAndType> getInputRequestContextKeysAsMap() {
    return this.inputRequestContextKeys.stream()
        .collect(Collectors.toMap(NameAndType::getName, Function.identity()));
  }

  /**
   * Returns the feature metadata as a {@link java.util.Map} with the (featureNamespace.featureName)
   * as the key
   */
  public Map<String, NameAndType> getFeatureValuesAsMap() {
    return this.featureValues.stream()
        .collect(Collectors.toMap(NameAndType::getName, Function.identity()));
  }

  /** Overrides <i>equals()</i> in class {@link Object} */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    FeatureServiceMetadata that = (FeatureServiceMetadata) o;
    return Objects.equals(inputJoinKeys, that.inputJoinKeys)
        && Objects.equals(inputRequestContextKeys, that.inputRequestContextKeys)
        && Objects.equals(featureValues, that.featureValues);
  }

  /** Overrides <i>hashCode()</i> in class {@link Object} */
  @Override
  public int hashCode() {
    return Objects.hash(inputJoinKeys, inputRequestContextKeys, featureValues);
  }
}
