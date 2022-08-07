package ai.tecton.client.response;

import ai.tecton.client.exceptions.TectonClientException;
import ai.tecton.client.exceptions.TectonErrorMessage;
import ai.tecton.client.model.FeatureValue;
import ai.tecton.client.model.SloInformation;
import ai.tecton.client.response.GetFeaturesResponseUtils.FeatureMetadata;
import ai.tecton.client.response.GetFeaturesResponseUtils.FeatureVectorJson;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import org.apache.commons.lang3.StringUtils;

/**
 * A class that represents the response from the HTTP API for a call to the <i>/get-features</i>
 * endpoint. The class provides methods to access the feature vector returned, along with its
 * metadata, if present.
 */
public class GetFeaturesResponse extends AbstractTectonResponse {

  private List<FeatureValue> featureValues;
  private SloInformation sloInformation;
  private final JsonAdapter<GetFeaturesResponseJson> jsonAdapter;

  public GetFeaturesResponse(String response, Duration requestLatency)
      throws TectonClientException {
    super(requestLatency);
    Moshi moshi = new Moshi.Builder().build();
    jsonAdapter = moshi.adapter(GetFeaturesResponseJson.class);
    this.featureValues = new ArrayList<>();
    buildResponseFromJson(response);
  }

  GetFeaturesResponse(
      List<FeatureValue> featureValues, SloInformation sloInformation, Duration requestLatency) {
    super(requestLatency);
    this.featureValues = featureValues;
    this.sloInformation = sloInformation;
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
    FeatureVectorJson result;
    ResponseMetadata metadata;

    static class ResponseMetadata {
      List<FeatureMetadata> features;
      SloInformation sloInfo;
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
    List<FeatureMetadata> featureMetadata = responseJson.metadata.features;

    // Construct Feature Value object from response
    this.featureValues =
        GetFeaturesResponseUtils.constructFeatureVector(featureVector, featureMetadata);

    // Construct Slo Info if present
    if (responseJson.metadata.sloInfo != null) {
      this.sloInformation = responseJson.metadata.sloInfo;
    }
  }

  void setFeatureValues(List<FeatureValue> featureValues) {
    this.featureValues = featureValues;
  }

  void setSloInformation(SloInformation sloInformation) {
    this.sloInformation = sloInformation;
  }
}
