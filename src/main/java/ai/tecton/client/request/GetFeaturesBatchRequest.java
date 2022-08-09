package ai.tecton.client.request;

import ai.tecton.client.exceptions.TectonClientException;
import ai.tecton.client.exceptions.TectonErrorMessage;
import ai.tecton.client.model.MetadataOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections4.ListUtils;

public class GetFeaturesBatchRequest {
  public static final int MAX_MICRO_BATCH_SIZE = 10;
  public static final int DEFAULT_MICRO_BATCH_SIZE = 5;

  List<? extends AbstractGetFeaturesRequest> requestList;
  List<String> requestBodyList;

  private final boolean isBatchRequest;

  GetFeaturesBatchRequest(
      List<GetFeaturesMicroBatchRequest> microBatchRequestList, boolean isBatchRequest) {
    this.isBatchRequest = isBatchRequest;
    requestList = microBatchRequestList;
  }

  GetFeaturesBatchRequest(List<GetFeaturesRequest> getFeaturesRequestList) {
    this.requestList = getFeaturesRequestList;
    this.isBatchRequest = false;
  }

  boolean isBatchRequest() {
    return this.isBatchRequest;
  }

  public List<? extends AbstractGetFeaturesRequest> getRequestList() {
    return this.requestList;
  }

  /**
   * A Builder class for building instances of {@link GetFeaturesBatchRequest} objects from values
   * configured by setters
   */
  public static class Builder {
    private String workspaceName;
    private String featureServiceName;
    private List<GetFeaturesRequestData> requestDataList;
    MetadataOption[] metadataOptionList;
    private int microBatchSize = DEFAULT_MICRO_BATCH_SIZE;

    /** Constructs an empty Builder */
    public Builder() {
      this.requestDataList = new ArrayList<>();
    }

    /**
     * Setter for workspaceName
     *
     * @param workspaceName Name of the workspace in which the Feature Service is defined
     * @return this Builder
     */
    public Builder workspaceName(String workspaceName) {
      this.workspaceName = workspaceName;
      return this;
    }

    /**
     * Setter for featureServiceName
     *
     * @param featureServiceName Name of the Feature Service for which feature vectors are being
     *     requested
     * @return this Builder
     */
    public Builder featureServiceName(String featureServiceName) {
      this.featureServiceName = featureServiceName;
      return this;
    }

    /**
     * Setter for a {@link java.util.List} of {@link GetFeaturesRequestData}
     *
     * @param requestDataList {@link java.util.List} of {@link GetFeaturesRequestData} objects with
     *     joinKeyMap and/or requestContextMap
     * @return this Builder
     * @throws TectonClientException when requestDataList is null or empty
     */
    public Builder requestDataList(List<GetFeaturesRequestData> requestDataList)
        throws TectonClientException {
      if (requestDataList == null || requestDataList.isEmpty()) {
        throw new TectonClientException(TectonErrorMessage.INVALID_REQUEST_DATA_LIST);
      }
      this.requestDataList = requestDataList;
      return this;
    }

    /**
     * Adds a single {@link GetFeaturesRequestData} object to the List
     *
     * @param requestData {@link GetFeaturesRequestData} object with joinKeyMap and/or
     *     requestContextMap
     * @return this Builder
     */
    public Builder addRequestData(GetFeaturesRequestData requestData) {
      this.requestDataList.add(requestData);
      return this;
    }

    /**
     * Setter for {@link MetadataOption}
     *
     * @param metadataOptions {@link MetadataOption} object with options for retrieving additional
     *     metadata about the feature values. Note if MetadataOption.ALL is included, all metadata
     *     will be requested. If MetadataOption.NONE is included, all other arguments will be
     *     ignored. By default, MetadataOption.NAME and MetadataOption.DATA_TYPE will be added to
     *     each request
     * @return this Builder
     */
    public Builder metadataOptions(MetadataOption... metadataOptions) {
      this.metadataOptionList = metadataOptions;
      return this;
    }

    /**
     * Setter for microBatchSize
     *
     * @param microBatchSize an int value between 1 and {@value #MAX_MICRO_BATCH_SIZE}. The client
     *     splits the GetFeaturesBatchRequest into multiple micro batches of this size and executes
     *     them parallely. By default, the microBatchSize is set to {@value
     *     #DEFAULT_MICRO_BATCH_SIZE} for best performance.
     * @return this Builder
     * @throws TectonClientException when the microBatchSize is out of bounds of [ 1, {@value
     *     #MAX_MICRO_BATCH_SIZE} ]
     */
    public Builder microBatchSize(int microBatchSize) throws TectonClientException {
      if (microBatchSize > MAX_MICRO_BATCH_SIZE || microBatchSize < 1) {
        throw new TectonClientException(
            String.format(TectonErrorMessage.INVALID_MICRO_BATCH_SIZE, 1, MAX_MICRO_BATCH_SIZE));
      }
      this.microBatchSize = microBatchSize;
      return this;
    }

    /**
     * Returns an instance of {@link GetFeaturesBatchRequest} created from the fields set on this
     * builder
     *
     * @return {@link GetFeaturesBatchRequest} object
     * @throws TectonClientException when requestDataList is invalid ( when any joinKeyMap or
     *     requestContextMap is null or empty)
     */
    public GetFeaturesBatchRequest build() throws TectonClientException {
      AbstractTectonRequest.validateRequestParameters(workspaceName, featureServiceName);
      requestDataList
          .parallelStream()
          .forEach(AbstractGetFeaturesRequest::validateRequestParameters);
      // For microBatchSize=1, create a List of individual GetFeaturesRequest objects to call the
      // /get-features endpoint
      if (microBatchSize == 1 || requestDataList.size() == 1) {
        List<GetFeaturesRequest> getFeaturesRequestList =
            requestDataList
                .parallelStream()
                .map(
                    requestData ->
                        new GetFeaturesRequest(
                            this.workspaceName,
                            this.featureServiceName,
                            requestData,
                            metadataOptionList))
                .collect(Collectors.toList());
        return new GetFeaturesBatchRequest(getFeaturesRequestList);
      } else {
        // For microBatchSize > 1, partition the requestDataList into n sublists of size
        // microBatchSize and create GetFeaturesMicroBatchRequest for each
        List<GetFeaturesMicroBatchRequest> microBatchRequestList =
            ListUtils.partition(requestDataList, microBatchSize)
                .parallelStream()
                .map(
                    requestData ->
                        new GetFeaturesMicroBatchRequest(
                            workspaceName, featureServiceName, requestData, metadataOptionList))
                .collect(Collectors.toList());
        return new GetFeaturesBatchRequest(microBatchRequestList, true);
      }
    }
  }
}
