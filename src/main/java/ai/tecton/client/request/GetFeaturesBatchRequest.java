package ai.tecton.client.request;

import ai.tecton.client.exceptions.TectonClientException;
import ai.tecton.client.exceptions.TectonErrorMessage;
import ai.tecton.client.model.MetadataOption;
import ai.tecton.client.transport.TectonHttpClient;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.ListUtils;

/**
 * A class that represents a batch request to retrieve a list of feature vectors from the feature
 * server, for a given workspaceName and featureServiceName. The class can be used to make parallel
 * requests to retrieve multiple feature vectors from the feature server API. The actual number of
 * concurrent calls depends on the ConnectionPool size and {@code maxParallelRequests}
 * configurations in the {@link ai.tecton.client.TectonClientOptions}.
 *
 * <p>GetFeaturesBatchRequest uses either the /get-features or the /get-features-batch endpoint
 * depending on the configuration {@code microBatchSize}. By default, the microBatchSize is set to
 * {@link RequestConstants#DEFAULT_MICRO_BATCH_SIZE}. It can be configured to any value in the range
 * [ 1, {@link RequestConstants#MAX_MICRO_BATCH_SIZE} ]
 *
 * <p>For a GetFeaturesBatchRequest with a {@link GetFeaturesRequestData} of size {@code n} and a
 * {@code microBatchSize} of 1, the client enqueues {@code n} HTTP calls to be sent parallely to the
 * /get-features endpoint. The client waits until all calls are complete or a specific time
 * (configured with {@code timeout}) has elapsed and returns a {@link List} of {@link
 * ai.tecton.client.response.GetFeaturesResponse} objects of size {@code n}.
 *
 * <p>For a GetFeaturesBatchRequest with a {@link GetFeaturesRequestData} of size {@code n} and a
 * {@code microBatchSize} of k where k is in the range [ 1, {@link
 * RequestConstants#MAX_MICRO_BATCH_SIZE} ], the client enqueues Math.ceil(n/k) microbatch requests
 * to be sent parallely to the /get-features-batch endpoint, waits until all microbatch requests are
 * complete or a specific configured timeout has elapsed and returns a {@link List} of {@link
 * ai.tecton.client.response.GetFeaturesResponse} objects of size {@code n}.
 */
public class GetFeaturesBatchRequest {

  private List<? extends AbstractGetFeaturesRequest> requestList;
  private final int microBatchSize;
  private final Duration timeout;
  private static final String BATCH_ENDPOINT = "/api/v1/feature-service/get-features-batch";
  private static JsonAdapter<GetFeaturesMicroBatchRequest.GetFeaturesRequestBatchJson> jsonAdapter =
      null;
  private String endpoint;
  private TectonHttpClient.HttpMethod method;
  private final Moshi moshi =
      new Moshi.Builder()
          .add(AbstractGetFeaturesRequest.SerializeNulls.JSON_ADAPTER_FACTORY)
          .build();

  /**
   * Constructor that creates a new GetFeaturesBatchRequest with the specified parameters. {@code
   * metadataOptions} defaults to {@link RequestConstants#DEFAULT_METADATA_OPTIONS} , {@code
   * microBatchSize} defaults to {@link RequestConstants#DEFAULT_MICRO_BATCH_SIZE} and {@code
   * timeout} defaults to None
   *
   * @param workspaceName Name of the workspace in which the Feature Service is defined
   * @param featureServiceName Name of the Feature Service for which the feature vectors are being
   *     requested
   * @param requestDataList a {@link List} of {@link GetFeaturesRequestData} object with joinKeyMap
   *     and/or requestContextMap
   * @throws TectonClientException when workspacename or featureServiceName is empty or null
   * @throws TectonClientException when requestDataList is invalid (null/empty or contains
   *     null/empty elements)
   */
  public GetFeaturesBatchRequest(
      String workspaceName,
      String featureServiceName,
      List<GetFeaturesRequestData> requestDataList) {
    this(
        workspaceName,
        featureServiceName,
        requestDataList,
        RequestConstants.DEFAULT_METADATA_OPTIONS,
        RequestConstants.DEFAULT_MICRO_BATCH_SIZE,
        RequestConstants.NONE_TIMEOUT);
  }

  /**
   * Constructor that creates a new GetFeaturesBatchRequest with the specified parameters. {@code
   * microBatchSize} defaults to {@link RequestConstants#DEFAULT_MICRO_BATCH_SIZE} and {@code
   * timeout} defaults to None
   *
   * @param workspaceName Name of the workspace in which the Feature Service is defined
   * @param featureServiceName Name of the Feature Service for which the feature vectors are being
   *     requested
   * @param requestDataList a {@link List} of {@link GetFeaturesRequestData} object with joinKeyMap
   *     and/or requestContextMap
   * @param metadataOptions A {@link Set} of {@link MetadataOption} for retrieving additional
   *     metadata about the feature values. Use {@link RequestConstants#ALL_METADATA_OPTIONS} to
   *     request all metadata and {@link RequestConstants#NONE_METADATA_OPTIONS} to request no
   *     metadata respectively. By default, {@link RequestConstants#DEFAULT_METADATA_OPTIONS} will
   *     be added to each request
   * @throws TectonClientException when workspaceName or featureServiceName is empty or null
   * @throws TectonClientException when requestDataList is invalid (null/empty or contains
   *     null/empty elements)
   */
  public GetFeaturesBatchRequest(
      String workspaceName,
      String featureServiceName,
      List<GetFeaturesRequestData> requestDataList,
      Set<MetadataOption> metadataOptions) {
    this(
        workspaceName,
        featureServiceName,
        requestDataList,
        metadataOptions,
        RequestConstants.DEFAULT_MICRO_BATCH_SIZE,
        RequestConstants.NONE_TIMEOUT);
  }

  /**
   * Constructor that creates a new GetFeaturesBatchRequest with the specified parameters. {@code
   * timeout} defaults to None.
   *
   * @param workspaceName Name of the workspace in which the Feature Service is defined
   * @param featureServiceName Name of the Feature Service for which the feature vectors are being
   *     requested
   * @param requestDataList a {@link List} of {@link GetFeaturesRequestData} object with joinKeyMap
   *     and/or requestContextMap
   * @param metadataOptions A {@link Set} of {@link MetadataOption} for retrieving additional
   *     metadata about the feature values. Use {@link RequestConstants#ALL_METADATA_OPTIONS} to
   *     request all metadata and {@link RequestConstants#NONE_METADATA_OPTIONS} to request no
   *     metadata respectively. By default, {@link RequestConstants#DEFAULT_METADATA_OPTIONS} will
   *     be added to each request
   * @param microBatchSize an int value between 1 and {@value
   *     RequestConstants#MAX_MICRO_BATCH_SIZE}. The client splits the GetFeaturesBatchRequest into
   *     multiple micro batches of this size and executes them parallely. By default, the
   *     microBatchSize is set to {@value RequestConstants#DEFAULT_MICRO_BATCH_SIZE}
   * @throws TectonClientException when workspaceName or featureServiceName is empty or null
   * @throws TectonClientException when requestDataList is invalid (null/empty or contains
   *     null/empty elements)
   */
  public GetFeaturesBatchRequest(
      String workspaceName,
      String featureServiceName,
      List<GetFeaturesRequestData> requestDataList,
      Set<MetadataOption> metadataOptions,
      int microBatchSize) {
    this(
        workspaceName,
        featureServiceName,
        requestDataList,
        metadataOptions,
        microBatchSize,
        RequestConstants.NONE_TIMEOUT);
  }

  /**
   * Constructor that creates a new GetFeaturesBatchRequest with the specified parameters
   *
   * @param workspaceName Name of the workspace in which the Feature Service is defined
   * @param featureServiceName Name of the Feature Service for which the feature vectors are being
   *     requested
   * @param requestDataList a {@link List} of {@link GetFeaturesRequestData} object with joinKeyMap
   *     and/or requestContextMap
   * @param metadataOptions metadataOptions A {@link Set} of {@link MetadataOption} for retrieving
   *     additional metadata about the feature values. Use {@link
   *     RequestConstants#ALL_METADATA_OPTIONS} to request all metadata and {@link
   *     RequestConstants#NONE_METADATA_OPTIONS} to request no metadata respectively. By default,
   *     {@link RequestConstants#DEFAULT_METADATA_OPTIONS} will be added to each request
   * @param microBatchSize an int value between 1 and {@value
   *     RequestConstants#MAX_MICRO_BATCH_SIZE}. The client splits the GetFeaturesBatchRequest into
   *     multiple micro batches of this size and executes them parallely. By default, the
   *     microBatchSize is set to {@value RequestConstants#DEFAULT_MICRO_BATCH_SIZE}
   * @param timeout The max time in {@link Duration} for which the client waits for the batch
   *     requests to complete before canceling the operation and returning the partial list of
   *     results.
   * @throws TectonClientException when workspacename or featureServiceName is empty or null
   * @throws TectonClientException when requestDataList is invalid (null/empty or contains
   *     null/empty elements)
   * @throws TectonClientException when the microBatchSize is out of bounds of [ 1, {@value
   *     RequestConstants#MAX_MICRO_BATCH_SIZE} ]
   */
  public GetFeaturesBatchRequest(
      String workspaceName,
      String featureServiceName,
      List<GetFeaturesRequestData> requestDataList,
      Set<MetadataOption> metadataOptions,
      int microBatchSize,
      Duration timeout) {
    validateParameters(workspaceName, featureServiceName, requestDataList, microBatchSize);
    this.timeout = timeout;

    if (microBatchSize > 1 && requestDataList.size() > 1) {
      // For batch requests, partition the requestDataList into n sublists of size
      // microBatchSize and create GetFeaturesMicroBatchRequest for each
      this.requestList =
          ListUtils.partition(requestDataList, microBatchSize)
              .parallelStream()
              .map(
                  requestData ->
                      new GetFeaturesMicroBatchRequest(
                          workspaceName, featureServiceName, requestData, metadataOptions))
              .collect(Collectors.toList());
      this.microBatchSize = microBatchSize;
      jsonAdapter = moshi.adapter(GetFeaturesMicroBatchRequest.GetFeaturesRequestBatchJson.class);
      this.endpoint = BATCH_ENDPOINT;
      this.method = TectonHttpClient.HttpMethod.POST;
    } else {
      // For microBatchSize=1, create a List of individual GetFeaturesRequest objects
      this.requestList =
          requestDataList
              .parallelStream()
              .map(
                  requestData ->
                      new GetFeaturesRequest(
                          workspaceName, featureServiceName, requestData, metadataOptions))
              .collect(Collectors.toList());
      this.microBatchSize = microBatchSize;
      this.endpoint = GetFeaturesRequest.ENDPOINT;
      this.method = TectonHttpClient.HttpMethod.POST;
    }
  }

  /**
   * Return Batch Request List
   *
   * @return {@link List of {@link AbstractGetFeaturesRequest} representing the list of batch
   *     request}
   */
  public List<? extends AbstractGetFeaturesRequest> getRequestList() {
    return this.requestList;
  }

  /**
   * Getter for timeout
   *
   * @return timeout in {@link Duration}
   */
  public Duration getTimeout() {
    return timeout;
  }

  /**
   * Getter for microBatchSize
   *
   * @return microBatchSize ( {@value RequestConstants#DEFAULT_MICRO_BATCH_SIZE} if not set
   */
  public int getMicroBatchSize() {
    return this.microBatchSize;
  }

  public TectonHttpClient.HttpMethod getMethod() {
    return method;
  }

  public String getEndpoint() {
    return endpoint;
  }

  /**
   * A Builder class for building instances of {@link GetFeaturesBatchRequest} objects from values
   * configured by setters
   */
  public static class Builder {
    private String workspaceName;
    private String featureServiceName;
    private List<GetFeaturesRequestData> requestDataList;
    private Set<MetadataOption> metadataOptionList = RequestConstants.DEFAULT_METADATA_OPTIONS;
    private int microBatchSize = RequestConstants.DEFAULT_MICRO_BATCH_SIZE;
    private Duration timeout = RequestConstants.NONE_TIMEOUT;

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
     * @param metadataOptions A {@link Set} of {@link MetadataOption} for retrieving additional
     *     metadata about the feature values. Use {@link RequestConstants#ALL_METADATA_OPTIONS} to
     *     request all metadata and {@link RequestConstants#NONE_METADATA_OPTIONS} to request no
     *     metadata respectively. By default, {@link RequestConstants#DEFAULT_METADATA_OPTIONS} will
     *     be added to each request
     * @return this Builder
     */
    public Builder metadataOptions(Set<MetadataOption> metadataOptions) {
      this.metadataOptionList = metadataOptions;
      return this;
    }

    /**
     * Setter for microBatchSize
     *
     * @param microBatchSize an int value between 1 and {@value
     *     RequestConstants#MAX_MICRO_BATCH_SIZE}. The client splits the GetFeaturesBatchRequest
     *     into multiple micro batches of this size and executes them parallely. By default, the
     *     microBatchSize is set to {@value RequestConstants#DEFAULT_MICRO_BATCH_SIZE}
     * @return this Builder
     * @throws TectonClientException when the microBatchSize is out of bounds of [ 1, {@value
     *     RequestConstants#MAX_MICRO_BATCH_SIZE} ]
     */
    public Builder microBatchSize(int microBatchSize) throws TectonClientException {
      this.microBatchSize = microBatchSize;
      return this;
    }

    /**
     * @param timeout The max time in {@link Duration} for which the client waits for the batch
     *     requests to complete before canceling the operation and returning the partial list of
     *     results.
     * @return this Builder
     */
    public Builder timeout(Duration timeout) {
      this.timeout = timeout;
      return this;
    }

    /**
     * Returns an instance of {@link GetFeaturesBatchRequest} created from the fields set on this
     * builder
     *
     * @return {@link GetFeaturesBatchRequest} object
     * @throws TectonClientException when requestDataList is invalid ( when the requestDataList is
     *     null or empty, or any joinKeyMap or requestContextMap is null or empty)
     * @throws TectonClientException when microBatchSize is out of bounds of [1, {@value
     *     RequestConstants#MAX_MICRO_BATCH_SIZE}
     */
    public GetFeaturesBatchRequest build() throws TectonClientException {
      return new GetFeaturesBatchRequest(
          workspaceName,
          featureServiceName,
          requestDataList,
          metadataOptionList,
          microBatchSize,
          timeout);
    }
  }

  // Validate request parameters
  private static void validateParameters(
      String workspaceName,
      String featureServiceName,
      List<GetFeaturesRequestData> requestDataList,
      int microBatchSize) {
    AbstractTectonRequest.validateRequestParameters(workspaceName, featureServiceName);
    if (requestDataList == null || requestDataList.isEmpty()) {
      throw new TectonClientException(TectonErrorMessage.INVALID_REQUEST_DATA_LIST);
    }
    requestDataList.parallelStream().forEach(AbstractGetFeaturesRequest::validateRequestParameters);
    if (microBatchSize > RequestConstants.MAX_MICRO_BATCH_SIZE || microBatchSize < 1) {
      throw new TectonClientException(
          String.format(
              TectonErrorMessage.INVALID_MICRO_BATCH_SIZE,
              1,
              RequestConstants.MAX_MICRO_BATCH_SIZE));
    }
  }

  // Moshi JSON Classes
  static class GetFeaturesMicroBatchRequest extends AbstractGetFeaturesRequest {

    private final List<GetFeaturesRequestData> requestDataList;

    GetFeaturesMicroBatchRequest(
        String workspaceName,
        String featureServiceName,
        List<GetFeaturesRequestData> requestDataList,
        Set<MetadataOption> metadataOptions) {
      super(workspaceName, featureServiceName, BATCH_ENDPOINT, metadataOptions);
      this.requestDataList = requestDataList;
    }

    // Moshi JSON classes
    static class GetFeaturesRequestBatchJson {
      GetFeaturesBatchFields params;

      GetFeaturesRequestBatchJson(GetFeaturesBatchFields params) {
        this.params = params;
      }
    }

    static class GetFeaturesBatchFields {
      String feature_service_name;
      String workspace_name;
      List<RequestDataField> request_data;
      Map<String, Boolean> metadata_options;
    }

    static class RequestDataField {
      @SerializeNulls Map<String, String> join_key_map;
      Map<String, Object> request_context_map;
    }

    List<GetFeaturesRequestData> getFeaturesRequestData() {
      return this.requestDataList;
    }

    // Convert MicroBatch Request to JSON String
    @Override
    public String requestToJson() {
      GetFeaturesBatchFields getFeaturesFields = new GetFeaturesBatchFields();
      getFeaturesFields.feature_service_name = this.getFeatureServiceName();
      getFeaturesFields.workspace_name = this.getWorkspaceName();
      getFeaturesFields.request_data = new ArrayList<>(this.requestDataList.size());
      this.requestDataList.forEach(
          requestData -> {
            RequestDataField requestDataField = new RequestDataField();
            if (!requestData.isEmptyJoinKeyMap()) {
              requestDataField.join_key_map = requestData.getJoinKeyMap();
            }
            if (!requestData.isEmptyRequestContextMap()) {
              requestDataField.request_context_map = requestData.getRequestContextMap();
            }
            getFeaturesFields.request_data.add(requestDataField);
          });
      if (!metadataOptions.isEmpty()) {
        getFeaturesFields.metadata_options =
            metadataOptions.stream()
                .collect(Collectors.toMap(MetadataOption::getJsonName, (a) -> Boolean.TRUE));
      }
      GetFeaturesRequestBatchJson getFeaturesRequestJson =
          new GetFeaturesRequestBatchJson(getFeaturesFields);
      try {
        return jsonAdapter.toJson(getFeaturesRequestJson);
      } catch (Exception e) {
        throw new TectonClientException(
            String.format(TectonErrorMessage.INVALID_GET_FEATURE_BATCH_REQUEST, e.getMessage()));
      }
    }
  }
}
