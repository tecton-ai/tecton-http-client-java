package ai.tecton.client.request;

import ai.tecton.client.exceptions.TectonClientException;
import ai.tecton.client.exceptions.TectonErrorMessage;
import ai.tecton.client.model.MetadataOption;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A subclass of {@link AbstractTectonRequest} that represents a request to the <i>/get-features</i>
 * endpoint to retrieve feature values from Tecton's online store
 */
public class GetFeaturesRequest extends AbstractGetFeaturesRequest {

  static final String ENDPOINT = "/api/v1/feature-service/get-features";
  private final JsonAdapter<GetFeaturesRequestJson> jsonAdapter;
  private final GetFeaturesRequestData getFeaturesRequestData;
  private final Moshi moshi = new Moshi.Builder().build();

  /**
   * Constructor that creates a new GetFeaturesRequest with specified parameters. {@code
   * metadataOptions} will default to {@link RequestConstants#DEFAULT_METADATA_OPTIONS}
   *
   * @param workspaceName Name of the workspace in which the Feature Service is defined
   * @param featureServiceName Name of the Feature Service for which the feature vector is being
   *     requested
   * @param getFeaturesRequestData {@link GetFeaturesRequestData} object with joinKeyMap and/or
   *     requestContextMap
   */
  public GetFeaturesRequest(
      String workspaceName,
      String featureServiceName,
      GetFeaturesRequestData getFeaturesRequestData) {

    super(workspaceName, featureServiceName, ENDPOINT, RequestConstants.DEFAULT_METADATA_OPTIONS);
    validateRequestParameters(getFeaturesRequestData);
    this.getFeaturesRequestData = getFeaturesRequestData;
    jsonAdapter = moshi.adapter(GetFeaturesRequestJson.class);
  }

  /**
   * Constructor that creates a new GetFeaturesRequest with the specified parameters
   *
   * @param workspaceName Name of the workspace in which the Feature Service is defined
   * @param featureServiceName Name of the Feature Service for which the feature vector is being
   *     requested
   * @param getFeaturesRequestData {@link GetFeaturesRequestData} object with joinKeyMap and/or
   *     requestContextMap
   * @param metadataOptions A {@link Set} of {@link MetadataOption} for retrieving additional
   *     metadata about the feature values. Use {@link RequestConstants#ALL_METADATA_OPTIONS} to
   *     request all metadata and {@link RequestConstants#NONE_METADATA_OPTIONS} to request no
   *     metadata respectively. By default, {@link RequestConstants#DEFAULT_METADATA_OPTIONS} will
   *     be added to each request
   */
  public GetFeaturesRequest(
      String workspaceName,
      String featureServiceName,
      GetFeaturesRequestData getFeaturesRequestData,
      Set<MetadataOption> metadataOptions) {

    super(workspaceName, featureServiceName, ENDPOINT, metadataOptions);
    validateRequestParameters(getFeaturesRequestData);
    this.getFeaturesRequestData = getFeaturesRequestData;
    jsonAdapter = moshi.adapter(GetFeaturesRequestJson.class);
  }

  GetFeaturesRequestData getFeaturesRequestData() {
    return this.getFeaturesRequestData;
  }

  static class GetFeaturesRequestJson {
    GetFeaturesFields params;

    GetFeaturesRequestJson(GetFeaturesFields params) {
      this.params = params;
    }
  }

  static class GetFeaturesFields {
    String feature_service_name;
    String workspace_name;
    Map<String, String> join_key_map;
    Map<String, Object> request_context_map;
    Map<String, Boolean> metadata_options;
  }

  @Override
  public String requestToJson() {
    GetFeaturesFields getFeaturesFields = new GetFeaturesFields();
    getFeaturesFields.feature_service_name = this.getFeatureServiceName();
    getFeaturesFields.workspace_name = this.getWorkspaceName();
    if (!getFeaturesRequestData().isEmptyJoinKeyMap()) {
      getFeaturesFields.join_key_map = getFeaturesRequestData().getJoinKeyMap();
    }
    if (!getFeaturesRequestData().isEmptyRequestContextMap()) {
      getFeaturesFields.request_context_map = getFeaturesRequestData().getRequestContextMap();
    }
    if (!metadataOptions.isEmpty()) {
      getFeaturesFields.metadata_options =
          metadataOptions.stream()
              .collect(Collectors.toMap(MetadataOption::getJsonName, (a) -> Boolean.TRUE));
    }
    GetFeaturesRequestJson getFeaturesRequestJson = new GetFeaturesRequestJson(getFeaturesFields);
    try {
      return jsonAdapter.toJson(getFeaturesRequestJson);
    } catch (Exception e) {
      throw new TectonClientException(
          String.format(TectonErrorMessage.INVALID_GET_FEATURE_REQUEST, e.getMessage()));
    }
  }
}
