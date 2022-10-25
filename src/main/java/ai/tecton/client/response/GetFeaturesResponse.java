package ai.tecton.client.response;

import ai.tecton.client.exceptions.TectonClientException;
import ai.tecton.client.exceptions.TectonErrorMessage;
import ai.tecton.client.model.FeatureStatus;
import ai.tecton.client.model.FeatureValue;
import ai.tecton.client.model.SloInformation;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 * A class that represents the response from the HTTP API for a call to the <i>/get-features</i>
 * endpoint. The class provides methods to access the feature vector returned, along with its
 * metadata, if present.
 */
public class GetFeaturesResponse extends AbstractTectonResponse {

  private final List<FeatureValue> featureValues;
  private SloInformation sloInformation;

  private final JsonAdapter<GetFeaturesResponseJson> jsonAdapter;
  private static final String NAME = "Name";
  private static final String DATA_TYPE = "Data Type";

  public GetFeaturesResponse(String response, Duration requestLatency)
      throws TectonClientException {
    super(requestLatency);
    Moshi moshi = new Moshi.Builder().build();
    jsonAdapter = moshi.adapter(GetFeaturesResponseJson.class);
    this.featureValues = new ArrayList<>();
    buildResponseFromJson(response);
  }

  // Package-Private constructor
  GetFeaturesResponse(List<FeatureValue> featureValues, Duration requestLatency) {
    super(requestLatency);
    this.featureValues = featureValues;
    Moshi moshi = new Moshi.Builder().build();
    jsonAdapter = moshi.adapter(GetFeaturesResponseJson.class);
  }

  /**
   * Returns the feature vector as a List of {@link FeatureValue} objects
   *
   * @return List of {@link FeatureValue}
   */
  public List<FeatureValue> getFeatureValues() {
    return featureValues;
  }

  /** Returns the feature vector as a Map, with the feature name as the key */
  public Map<String, FeatureValue> getFeatureValuesAsMap() {
    Map<String, FeatureValue> featureMap = new HashMap<>();
    featureValues.forEach(
        featureValue ->
            featureMap.put(
                StringUtils.join(
                    featureValue.getFeatureNamespace(), ".", featureValue.getFeatureName()),
                featureValue));
    return featureMap;
  }

  /**
   * Returns an {@link SloInformation} objected wrapped in {@link java.util.Optional} if present in
   * the response received from the HTTP API, Optional.empty() otherwise
   */
  public Optional<SloInformation> getSloInformation() {
    return Optional.ofNullable(sloInformation);
  }

  static class GetFeaturesResponseJson {
    Result result;
    ResponseMetadata metadata;

    static class Result {
      List<Object> features;
    }

    static class ResponseMetadata {
      List<FeatureMetadata> features;
      SloInformation sloInfo;
    }

    static class FeatureMetadata {
      String name;
      String effectiveTime;
      ResponseDataType dataType = new ResponseDataType();
      String status;
    }
  }

  @Override
  void buildResponseFromJson(String response) {
    GetFeaturesResponseJson responseJson;
    try {
      responseJson = jsonAdapter.fromJson(response);
    } catch (IOException e) {
      throw new TectonClientException(TectonErrorMessage.INVALID_RESPONSE_FORMAT);
    }
    List<Object> featureVector = responseJson.result.features;
    List<GetFeaturesResponseJson.FeatureMetadata> featureMetadata = responseJson.metadata.features;

    validateResponse(featureVector, featureMetadata);
    // Construct Feature Value object from response
    for (int i = 0; i < responseJson.result.features.size(); i++) {
      ArrayList<Optional<FeatureStatus>> featureStatuses = null;
      if (featureMetadata.get(i).status != null) {
        featureStatuses = new ArrayList<>();
        featureStatuses.add(FeatureStatus.fromString(featureMetadata.get(i).status));
      }
      FeatureValue value =
          new FeatureValue(
              featureVector.get(i),
              featureMetadata.get(i).name,
              featureMetadata.get(i).dataType.getDataType(),
              featureMetadata.get(i).dataType.getListElementType(),
              featureMetadata.get(i).effectiveTime,
              Optional.ofNullable(featureStatuses));
      this.featureValues.add(value);
    }
    // Construct Slo Info if present
    if (responseJson.metadata.sloInfo != null) {
      this.sloInformation = responseJson.metadata.sloInfo;
    }
  }

  void setSloInformation(SloInformation sloInformation) {
    this.sloInformation = sloInformation;
  }

  private void validateResponse(
      List<Object> featureVector, List<GetFeaturesResponseJson.FeatureMetadata> featureMetadata) {
    if (featureVector.isEmpty()) {
      throw new TectonClientException(TectonErrorMessage.EMPTY_FEATURE_VECTOR);
    }
    for (GetFeaturesResponseJson.FeatureMetadata metadata : featureMetadata) {
      if (StringUtils.isEmpty(metadata.name)) {
        throw new TectonClientException(
            String.format(TectonErrorMessage.MISSING_EXPECTED_METADATA, NAME));
      }
      if (StringUtils.isEmpty(metadata.dataType.type)) {
        {
          throw new TectonClientException(
              String.format(TectonErrorMessage.MISSING_EXPECTED_METADATA, DATA_TYPE));
        }
      }
    }
  }
}
