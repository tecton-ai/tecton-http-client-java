package ai.tecton.client.request;

import ai.tecton.client.exceptions.TectonClientException;
import ai.tecton.client.exceptions.TectonErrorMessage;
import ai.tecton.client.model.MetadataOption;
import ai.tecton.client.transport.TectonHttpClient;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A subclass of {@link AbstractTectonRequest} that represents a request to the
 * <i>/get-features-batch</i> endpoint to retrieve batch feature values from Tecton's online store
 */
public class GetFeaturesBatchRequest extends AbstractTectonRequest {

  private static final String ENDPOINT = "/api/v1/feature-service/get-features-batch";
  private static final int MAX_MICRO_BATCH_SIZE = 10;
  private static final int DEFAULT_MICRO_BATCH_SIZE = 5;
  private static final TectonHttpClient.HttpMethod httpMethod = TectonHttpClient.HttpMethod.POST;
  private final JsonAdapter<GetFeaturesRequestBatchJson> jsonAdapter;
  private final List<GetFeaturesRequestData> getFeaturesRequestDataList;
  private final Set<MetadataOption> metadataOptions;
  private int microBatchSize;

  /**
   * Constructor that creates a new GetFeaturesBatchRequest with specified parameters and default
   * MetadataOptions.
   *
   * <p>Note: Each GetFeaturesBatchRequest will always include the MetadataOption.NAME and
   * MetadataOption.DATA_TYPE options
   *
   * @param workspaceName Name of the workspace in which the Feature Service is defined
   * @param featureServiceName Name of the Feature Service for which the feature vectors are being
   *     requested
   * @param getFeaturesRequestDataList A java.util.List of {@link GetFeaturesRequestData} objects
   *     with joinKeyMaps and/or requestContextMaps
   */
  public GetFeaturesBatchRequest(
      String workspaceName,
      String featureServiceName,
      List<GetFeaturesRequestData> getFeaturesRequestDataList) {
    super(ENDPOINT, httpMethod, workspaceName, featureServiceName);
    getFeaturesRequestDataList.forEach(GetFeaturesUtils::validateRequestParameters);
    this.getFeaturesRequestDataList = getFeaturesRequestDataList;
    this.metadataOptions = GetFeaturesUtils.defaultMetadataOptions;
    Moshi moshi = new Moshi.Builder().build();
    jsonAdapter = moshi.adapter(GetFeaturesRequestBatchJson.class);
    microBatchSize = DEFAULT_MICRO_BATCH_SIZE;
  }

  /**
   * Constructor that creates a new GetFeaturesBatchRequest with the specified parameters
   *
   * @param workspaceName Name of the workspace in which the Feature Service is defined
   * @param featureServiceName featureServiceName Name of the Feature Service for which the feature
   *     vectors are being requested
   * @param getFeaturesRequestDataList java.util.List of {@link GetFeaturesRequestData} objects with
   *     joinKeyMaps and/or requestContextMaps
   * @param metadataOptions {@link MetadataOption} varargs for retrieving additional metadata about
   *     the feature values.
   *     <p>Note if MetadataOption.ALL is included, all metadata will be requested. If
   *     MetadataOption.NONE is included, all other arguments will be ignored. By default,
   *     MetadataOption.NAME and MetadataOption.DATA_TYPE will be added to each request
   */
  public GetFeaturesBatchRequest(
      String workspaceName,
      String featureServiceName,
      List<GetFeaturesRequestData> getFeaturesRequestDataList,
      MetadataOption... metadataOptions) {
    super(ENDPOINT, httpMethod, workspaceName, featureServiceName);
    getFeaturesRequestDataList.forEach(GetFeaturesUtils::validateRequestParameters);
    this.getFeaturesRequestDataList = getFeaturesRequestDataList;
    this.metadataOptions = GetFeaturesUtils.getMetadataOptions(metadataOptions);
    Moshi moshi = new Moshi.Builder().build();
    jsonAdapter = moshi.adapter(GetFeaturesRequestBatchJson.class);
    microBatchSize = DEFAULT_MICRO_BATCH_SIZE;
  }

  /**
   * Setter for microbatch size for GetFeaturesBatch requests.
   *
   * @param microBatchSize The micro batch size for GetFeaturesBatch requests
   * @return GetFeaturesBatchRequest object after setting the microBatchSize
   * @throws TectonClientException when the microBatchSize exceeds the MAX value
   */
  public GetFeaturesBatchRequest setMicroBatchSize(int microBatchSize)
      throws TectonClientException {
    if (microBatchSize > MAX_MICRO_BATCH_SIZE) {
      throw new TectonClientException(
          String.format(TectonErrorMessage.EXCEEDS_MAX_BATCH_SIZE, MAX_MICRO_BATCH_SIZE));
    }
    this.microBatchSize = microBatchSize;
    return this;
  }

  /**
   * Get configured micro batch size foir the GetFeaturesBatchRequest
   *
   * @return microBatchSize
   */
  public int getMicroBatchSize() {
    return microBatchSize;
  }

  List<GetFeaturesRequestData> getFeaturesRequestDataList() {
    return this.getFeaturesRequestDataList;
  }

  Set<MetadataOption> getMetadataOptions() {
    return this.metadataOptions;
  }

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
    Map<String, String> join_key_map;
    Map<String, Object> request_context_map;
  }

  @Override
  public String requestToJson() {
    GetFeaturesBatchFields getFeaturesFields = new GetFeaturesBatchFields();
    getFeaturesFields.feature_service_name = this.getFeatureServiceName();
    getFeaturesFields.workspace_name = this.getWorkspaceName();
    getFeaturesFields.request_data = new ArrayList<>(this.getFeaturesRequestDataList().size());
    this.getFeaturesRequestDataList()
        .forEach(
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
