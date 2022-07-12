package com.tecton.client.request;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.tecton.client.exceptions.TectonClientException;
import com.tecton.client.exceptions.TectonErrorMessage;
import com.tecton.client.transport.TectonHttpClient.HttpMethod;

/**
 * A subclass of {@link com.tecton.client.request.AbstractTectonRequest} that represents a request
 * to the <i>feature-service/metadata</i> endpoint that returns metadata about a FeatureService,
 * including the schema for join keys and request context, as well as the in-order output schema of
 * returned feature values.
 */
public class GetFeatureServiceMetadataRequest extends AbstractTectonRequest {

  private static final HttpMethod method = HttpMethod.POST;
  private static final String ENDPOINT = "/api/v1/feature-service/metadata";
  private static final String DEFAULT_WORKSPACE = "prod";
  private JsonAdapter<GetFeatureServiceMetadataJson> jsonAdapter;

  /**
   * Constructor that creates a new GetFeatureServiceMetadataRequest with the specified
   * workspaceName and featureServiceName
   *
   * @param featureServiceName Name of the Feature Service for which the metadata is being requested
   * @param workspaceName Name of the workspace in which the Feature Service is defined
   */
  public GetFeatureServiceMetadataRequest(String featureServiceName, String workspaceName) {
    super(ENDPOINT, method, workspaceName, featureServiceName);
    Moshi moshi = new Moshi.Builder().build();
    jsonAdapter = moshi.adapter(GetFeatureServiceMetadataJson.class);
  }

  /**
   * Constructor that creates a new GetFeatureServiceMetadataRequest with default workspaceName
   * "prod" and featureServiceName provided.
   *
   * @param featureServiceName Name of the Feature Service for which the metadata is being requested
   */
  public GetFeatureServiceMetadataRequest(String featureServiceName) {
    super(ENDPOINT, method, DEFAULT_WORKSPACE, featureServiceName);
    Moshi moshi = new Moshi.Builder().build();
    jsonAdapter = moshi.adapter(GetFeatureServiceMetadataJson.class);
  }

  static class GetFeatureServiceMetadataJson {
    GetFeatureServiceMetadataFields params;

    GetFeatureServiceMetadataJson(GetFeatureServiceMetadataFields params) {
      this.params = params;
    }
  }

  static class GetFeatureServiceMetadataFields {
    String feature_service_name;
    String workspace_name;
  }

  @Override
  public String requestToJson() {
    GetFeatureServiceMetadataFields serviceMetadataFields = new GetFeatureServiceMetadataFields();
    serviceMetadataFields.feature_service_name = super.getFeatureServiceName();
    serviceMetadataFields.workspace_name = super.getWorkspaceName();

    try {
      return jsonAdapter.toJson(new GetFeatureServiceMetadataJson(serviceMetadataFields));
    } catch (Exception e) {
      throw new TectonClientException(
          String.format(TectonErrorMessage.INVALID_GET_SERVICE_METADATA_REQUEST, e.getMessage()));
    }
  }
}
