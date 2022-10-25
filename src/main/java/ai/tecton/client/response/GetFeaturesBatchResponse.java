package ai.tecton.client.response;

import ai.tecton.client.exceptions.TectonClientException;
import ai.tecton.client.exceptions.TectonErrorMessage;
import ai.tecton.client.exceptions.TectonServiceException;
import ai.tecton.client.model.FeatureValue;
import ai.tecton.client.model.SloInformation;
import ai.tecton.client.response.GetFeaturesResponseUtils.FeatureMetadata;
import ai.tecton.client.response.GetFeaturesResponseUtils.FeatureVectorJson;
import ai.tecton.client.transport.HttpResponse;
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
 *
 * <p>The {@link List} of {@link GetFeaturesResponse} objects represents the list of response, each
 * of which encapsulates a feature vector and its metadata. Note: The list may contain nulls for any
 * request that was never completed, due to a timeout.
 *
 * <p>The batchSloInformation is only present for batch requests to the /get-features-batch endpoint
 * (i.e. microBatchSize&gt;1)
 */
public class GetFeaturesBatchResponse {
  private final List<GetFeaturesResponse> batchResponseList;

  private SloInformation batchSloInfo;
  private Duration requestLatency;
  private static JsonAdapter<GetFeaturesMicroBatchResponse.GetFeaturesBatchResponseJson>
      jsonAdapter;

  public GetFeaturesBatchResponse(
      List<HttpResponse> httpResponseList, Duration totalDuration, int microBatchSize) {
    Moshi moshi = new Moshi.Builder().build();
    jsonAdapter = moshi.adapter(GetFeaturesMicroBatchResponse.GetFeaturesBatchResponseJson.class);

    // Serialize list of HttpResponse into list of GetFeaturesMicroBatchResponse
    List<GetFeaturesMicroBatchResponse> microBatchResponses =
        httpResponseList
            .parallelStream()
            .map(httpResponse -> parseSingleHttpResponse(httpResponse, microBatchSize))
            .collect(Collectors.toList());

    // Concatenate list of GetFeaturesResponse objects from each microbatch into a single list
    // Maintain ordering
    this.batchResponseList =
        microBatchResponses
            .parallelStream()
            .map(microBatch -> microBatch.microBatchResponseList)
            .flatMap(List::stream)
            .collect(Collectors.toList());

    // Compute Batch SLO Information, if present
    List<SloInformation> microBatchSloInfoList =
        microBatchResponses.stream()
            .filter(
                microBatchResponse -> microBatchResponse.getMicroBatchSloInformation().isPresent())
            .map(microBatchResponse -> microBatchResponse.getMicroBatchSloInformation().get())
            .collect(Collectors.toList());
    if (!microBatchSloInfoList.isEmpty()) {
      this.batchSloInfo = computeBatchSloInfo(microBatchSloInfoList);
    }
    this.requestLatency = totalDuration;
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
   * Returns the response time (network latency + online store latency) as provided by the
   * underlying Http Client
   *
   * @return response time as {@link java.time.Duration}
   */
  public Duration getRequestLatency() {
    return this.requestLatency;
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

  // Parse a single HttpResponse and extract GetFeaturesResponse, SloInformation
  // This method is called parallely for all responses in the list
  private GetFeaturesMicroBatchResponse parseSingleHttpResponse(
      HttpResponse httpResponse, int microBatchSize) {
    // Null HttpResponse represents a timeout and so all the individual responses in the microbatch
    // will be null
    if (httpResponse == null)
      return new GetFeaturesMicroBatchResponse(Collections.nCopies(microBatchSize, null), null);
    // For an error response, throw TectonServiceException
    if (!httpResponse.isSuccessful()) {
      throw new TectonServiceException(
          String.format(
              TectonErrorMessage.ERROR_RESPONSE,
              httpResponse.getResponseCode(),
              httpResponse.getMessage()));
    }
    // Response is not null, but response body is empty.
    if (!httpResponse.getResponseBody().isPresent()) {
      throw new TectonClientException(TectonErrorMessage.EMPTY_RESPONSE);
    }

    String responseJson = httpResponse.getResponseBody().get();
    if (microBatchSize == 1) {
      return new GetFeaturesMicroBatchResponse(
          Collections.singletonList(
              new GetFeaturesResponse(responseJson, httpResponse.getRequestDuration())),
          null);
    } else {
      return new GetFeaturesMicroBatchResponse(responseJson, httpResponse.getRequestDuration());
    }
  }

  private static class GetFeaturesMicroBatchResponse extends AbstractTectonResponse {
    private List<GetFeaturesResponse> microBatchResponseList;
    private SloInformation microBatchSloInfo;

    GetFeaturesMicroBatchResponse(String response, Duration requestLatency) {
      super(requestLatency);
      buildResponseFromJson(response);
    }

    GetFeaturesMicroBatchResponse(
        List<GetFeaturesResponse> microBatchResponseList, Duration requestDuration) {
      super(requestDuration);
      this.microBatchResponseList = microBatchResponseList;
    }

    Optional<SloInformation> getMicroBatchSloInformation() {
      return Optional.ofNullable(this.microBatchSloInfo);
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
  SloInformation computeBatchSloInfo(List<SloInformation> batchSloInformation) {
    batchSloInformation.removeAll(Collections.singleton(null));

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

    return new SloInformation.Builder()
        .isSloEligible(isSloEligibleBatch)
        .serverTimeSeconds(maxServerTimeSeconds)
        .sloServerTimeSeconds(maxSloServerTimeSeconds)
        .sloIneligibilityReasons(sloIneligibilityReasons)
        .storeMaxLatency(storeMaxLatency)
        .build();
  }

  private Double getMaxValueFromOptionalList(List<Optional<Double>> values) {
    OptionalDouble doubleVal =
        values.stream().filter(Optional::isPresent).mapToDouble(Optional::get).max();
    return doubleVal.isPresent() ? doubleVal.getAsDouble() : null;
  }
}
