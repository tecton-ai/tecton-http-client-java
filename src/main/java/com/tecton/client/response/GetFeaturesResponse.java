package com.tecton.client.response;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.tecton.client.exceptions.TectonClientException;
import com.tecton.client.exceptions.TectonErrorMessage;
import com.tecton.client.model.FeatureValue;
import com.tecton.client.model.SloInformation;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.time.Duration;
import java.util.*;

public class GetFeaturesResponse {

  private Map<String, FeatureValue> featureValues;
  private Duration requestLatency;
  private SloInformation sloInformation;
  JsonAdapter<GetFeaturesResponseJson> jsonAdapter;
  private static final String NAME = "Name";
  private static final String DATA_TYPE = "Data Type";

  public GetFeaturesResponse(String response, Duration requestLatency) {
    Moshi moshi = new Moshi.Builder().build();
    jsonAdapter = moshi.adapter(GetFeaturesResponseJson.class);
    this.featureValues = new HashMap<>();
    this.requestLatency = requestLatency;
    buildResponseFromJson(response);
  }

  public List<FeatureValue> getFeatureValues() {
    return new ArrayList<>(featureValues.values());
  }

  public Map<String, FeatureValue> getFeatureValuesAsMap() {
    return featureValues;
  }

  public Duration getRequestLatency() {
    return requestLatency;
  }

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
      String effective_time;
      TectonDataType data_type = new TectonDataType();

      FeatureMetadata() {}
    }

    static class TectonDataType {
      String type;
      TectonDataType element_type;
    }
  }

  public void buildResponseFromJson(String response) {
    GetFeaturesResponseJson responseJson;
    try {
      responseJson = jsonAdapter.fromJson(response);
    } catch (IOException e) {
      throw new TectonClientException(TectonErrorMessage.INVALID_RESPONSE_FORMAT);
    }
    List<Object> featureVector = responseJson.result.features;
    List<GetFeaturesResponseJson.FeatureMetadata> featureMetadata = responseJson.metadata.features;

    validateResponse(featureVector, featureMetadata);

    for (int i = 0; i < responseJson.result.features.size(); i++) {
      GetFeaturesResponseJson.TectonDataType elementTypeMap =
          featureMetadata.get(i).data_type.element_type;
      Optional<String> listElementType =
          elementTypeMap == null ? Optional.empty() : Optional.ofNullable(elementTypeMap.type);
      FeatureValue value =
          new FeatureValue(
              featureVector.get(i),
              featureMetadata.get(i).name,
              featureMetadata.get(i).data_type.type,
              listElementType,
              featureMetadata.get(i).effective_time);
      this.featureValues.put(value.getRelativeFeatureName(), value);
    }
    if (responseJson.metadata.sloInfo != null) {
      this.sloInformation = responseJson.metadata.sloInfo;
    }
  }

  private void validateResponse(
      List<Object> featureVector, List<GetFeaturesResponseJson.FeatureMetadata> featureMetadata) {
    if (featureVector.isEmpty()) {
      // TODO Is an empty feature vector an error?
    }
    if (featureVector.size() != featureMetadata.size()) {
      throw new TectonClientException(TectonErrorMessage.MISMATCHED_FEATURE_VECTOR_SIZE);
    }
    for (GetFeaturesResponseJson.FeatureMetadata metadata : featureMetadata) {
      if (StringUtils.isEmpty(metadata.name)) {
        throw new TectonClientException(
            String.format(TectonErrorMessage.MISSING_EXPECTED_METADATA, NAME));
      }
      if (StringUtils.isEmpty(metadata.data_type.type)) {
        throw new TectonClientException(
            String.format(TectonErrorMessage.MISSING_EXPECTED_METADATA, DATA_TYPE));
      }
    }
  }
}
