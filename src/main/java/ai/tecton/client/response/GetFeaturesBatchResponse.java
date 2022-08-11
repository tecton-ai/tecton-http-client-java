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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A class that represents the response from the HTTP API for when fetching batch features. The
 * class provides methods to access the list of feature vector returned, along with its metadata, if
 * present.
 */
public class GetFeaturesBatchResponse {
  private List<GetFeaturesResponse> batchResponseList;
  private SloInformation batchSloInfo;
  private Duration requestLatency;

  public GetFeaturesBatchResponse(
      Map<String, Duration> responseListWithLatency, boolean isBatchRequest) {

    if (isBatchRequest) {
      // Extract List<GetFeaturesResponse> from list of batch response strings
      List<GetFeaturesMicroBatchResponse> microBatchResponses =
          responseListWithLatency
              .entrySet()
              .parallelStream()
              .map(entry -> new GetFeaturesMicroBatchResponse(entry.getKey(), entry.getValue()))
              .collect(Collectors.toList());
      this.batchResponseList =
          microBatchResponses.stream()
              .map(microBatch -> microBatch.microBatchResponseList)
              .flatMap(Collection::stream)
              .collect(Collectors.toList());
      // Compute Batch SLO Information
      List<SloInformation> batchSloInfoList =
          microBatchResponses.stream()
              .map(GetFeaturesMicroBatchResponse::getMicroBatchSloInformation)
              .collect(Collectors.toList());
      this.batchSloInfo = computeBatchSloInfo(batchSloInfoList).orElse(null);
    } else {
      // Extract List<GetFeaturesResponse> from list of response strings
      this.batchResponseList =
          responseListWithLatency
              .entrySet()
              .parallelStream()
              .map(
                  entry ->
                      entry.getKey() == null
                          ? null
                          : new GetFeaturesResponse(entry.getKey(), entry.getValue()))
              .collect(Collectors.toList());
    }
    this.requestLatency = Collections.max(responseListWithLatency.values());
  }

  /**
   * Returns a list of {@link GetFeaturesResponse} objects, each encapsulating a feature vector and
   * its metadata
   *
   * @return {@link List} of {@link GetFeaturesResponse}
   */
  public List<GetFeaturesResponse> getBatchResponseList() {
    return batchResponseList;
  }

  /**
   * Returns an {@link SloInformation} object wrapped in {@link java.util.Optional} if present in
   * the response received from the HTTP API, Optional.empty() otherwise
   *
   * @return {@link SloInformation} for the batch request
   */
  public Optional<SloInformation> getBatchSloInformation() {
    return Optional.ofNullable(this.batchSloInfo);
  }

  private static class GetFeaturesMicroBatchResponse extends AbstractTectonResponse {
    private List<GetFeaturesResponse> microBatchResponseList;
    private SloInformation microBatchSloInfo;
    private final JsonAdapter<GetFeaturesBatchResponseJson> jsonAdapter;

    GetFeaturesMicroBatchResponse(String response, Duration requestLatency) {
      super(requestLatency);
      Moshi moshi = new Moshi.Builder().build();
      jsonAdapter = moshi.adapter(GetFeaturesBatchResponseJson.class);
      buildResponseFromJson(response);
    }

    SloInformation getMicroBatchSloInformation() {
      return this.microBatchSloInfo;
    }

    // Moshi Json Classes
    static class GetFeaturesBatchResponseJson {
      List<GetFeaturesResponseUtils.FeatureVectorJson> result;
      ResponseMetadataJson metadata;

      static class ResponseMetadataJson {
        List<GetFeaturesResponseUtils.FeatureMetadata> features;
        List<SloInformation> sloInfo;
        SloInformation batchSloInfo;
      }
    }

    @Override
    void buildResponseFromJson(String response) {
      if (response != null) {
        GetFeaturesBatchResponseJson responseJson;
        try {
          responseJson = jsonAdapter.fromJson(response);
        } catch (IOException e) {
          throw new TectonClientException(TectonErrorMessage.INVALID_RESPONSE_FORMAT);
        }

        List<FeatureVectorJson> featureVectorJson = responseJson.result;
        List<FeatureMetadata> featureMetadata = responseJson.metadata.features;
        List<SloInformation> sloInformationList = responseJson.metadata.sloInfo;

        // Parallel Stream to map each feature vector and sloInfo (if present) in the response to a
        // corresponding
        // GetFeaturesResponse object and collect to a List
        // Preserves ordering
        this.microBatchResponseList =
            IntStream.range(0, responseJson.result.size())
                .parallel()
                .mapToObj(
                    i ->
                        generateGetFeaturesResponse(
                            featureVectorJson,
                            featureMetadata,
                            sloInformationList,
                            this.getRequestLatency(),
                            i))
                .collect(Collectors.toList());

        this.microBatchSloInfo = responseJson.metadata.batchSloInfo;
      }
    }
  }

  // Generate a single GetFeaturesResponse
  private static GetFeaturesResponse generateGetFeaturesResponse(
      List<FeatureVectorJson> featureVectorJson,
      List<FeatureMetadata> featureMetadata,
      List<SloInformation> sloInformationList,
      Duration requestLatency,
      int index) {

    List<FeatureValue> featureValues =
        GetFeaturesResponseUtils.constructFeatureVector(
            featureVectorJson.get(index).features, featureMetadata);
    GetFeaturesResponse getFeaturesResponse =
        new GetFeaturesResponse(featureValues, requestLatency);
    if (sloInformationList != null) {
      getFeaturesResponse.setSloInformation(sloInformationList.get(index));
    }
    return getFeaturesResponse;
  }

  // Compute Batch SLO Information
  Optional<SloInformation> computeBatchSloInfo(List<SloInformation> batchSloInformation) {
    batchSloInformation.removeAll(Collections.singleton(null));
    if (!batchSloInformation.isEmpty()) {

      boolean isSloEligibleBatch =
          batchSloInformation.stream()
              .noneMatch(
                  sloInfo -> sloInfo.isSloEligible().isPresent() && !sloInfo.isSloEligible().get());

      Double maxSloServerTimeSeconds =
          getMaxValueFromOptionalList(
              batchSloInformation.stream()
                  .map(SloInformation::getSloServerTimeSeconds)
                  .collect(Collectors.toList()));

      Double storeMaxLatency =
          getMaxValueFromOptionalList(
              batchSloInformation.stream()
                  .map(SloInformation::getStoreMaxLatency)
                  .collect(Collectors.toList()));

      Double maxServerTimeSeconds =
          getMaxValueFromOptionalList(
              batchSloInformation.stream()
                  .map(SloInformation::getServerTimeSeconds)
                  .collect(Collectors.toList()));

      Set<SloInformation.SloIneligibilityReason> sloIneligibilityReasons =
          batchSloInformation.stream()
              .map(SloInformation::getSloIneligibilityReasons)
              .flatMap(Collection::stream)
              .collect(Collectors.toSet());

      return Optional.of(
          SloInformation.buildSloInformation(
              isSloEligibleBatch,
              maxServerTimeSeconds,
              maxSloServerTimeSeconds,
              null,
              sloIneligibilityReasons,
              storeMaxLatency));
    }
    return Optional.empty();
  }

  private Double getMaxValueFromOptionalList(List<Optional<Double>> values) {
    OptionalDouble doubleVal =
        values.stream().filter(Optional::isPresent).mapToDouble(Optional::get).max();
    return doubleVal.isPresent() ? doubleVal.getAsDouble() : null;
  }
}
