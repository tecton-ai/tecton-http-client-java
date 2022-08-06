package ai.tecton.client.request;

import ai.tecton.client.exceptions.TectonClientException;
import ai.tecton.client.exceptions.TectonErrorMessage;
import ai.tecton.client.model.MetadataOption;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A subclass of {@link AbstractTectonRequest} that represents a request to the
 * <i>/get-features-batch</i> endpoint to retrieve batch feature values from Tecton's online store
 */
class GetFeaturesMicroBatchRequest extends AbstractGetFeaturesRequest {

  private static final String ENDPOINT = "/api/v1/feature-service/get-features-batch";

  private final JsonAdapter<GetFeaturesRequestBatchJson> jsonAdapter;
  private final List<GetFeaturesRequestData> requestDataList;

  GetFeaturesMicroBatchRequest(
      String workspaceName,
      String featureServiceName,
      List<GetFeaturesRequestData> requestDataList,
      MetadataOption... metadataOptions) {
    super(workspaceName, featureServiceName, ENDPOINT, metadataOptions);
    this.requestDataList = requestDataList;
    Moshi moshi = new Moshi.Builder().build();
    jsonAdapter = moshi.adapter(GetFeaturesRequestBatchJson.class);
  }

  List<GetFeaturesRequestData> getFeaturesRequestDataList() {
    return this.requestDataList;
  }

  // Moshi JSON class
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
