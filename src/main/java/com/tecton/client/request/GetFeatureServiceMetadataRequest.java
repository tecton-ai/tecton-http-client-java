package com.tecton.client.request;

import com.squareup.moshi.JsonAdapter;
import com.tecton.client.exceptions.TectonClientException;
import com.tecton.client.exceptions.TectonErrorMessage;
import com.tecton.client.transport.TectonHttpClient.HttpMethod;

public class GetFeatureServiceMetadataRequest extends AbstractTectonRequest {

  private static final HttpMethod method = HttpMethod.POST;
  private static final String ENDPOINT = "/api/v1/feature-service/metadata";
  private static final String DEFAULT_WORKSPACE = "prod";
  private JsonAdapter<GetFeatureServiceMetadataJson> jsonAdapter;

  public GetFeatureServiceMetadataRequest(String featureServiceName, String workspaceName) {
    super(ENDPOINT, method, workspaceName, featureServiceName);
  }

  public GetFeatureServiceMetadataRequest(String featureServiceName) {
    super(ENDPOINT, method, DEFAULT_WORKSPACE, featureServiceName);
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

  public String requestToJson() {
    GetFeatureServiceMetadataFields serviceMetadataFields = new GetFeatureServiceMetadataFields();
    serviceMetadataFields.feature_service_name = super.getFeatureServiceName();
    serviceMetadataFields.workspace_name = super.getWorkspaceName();

    try {
      return jsonAdapter.toJson(new GetFeatureServiceMetadataJson(serviceMetadataFields));
    } catch (Exception e) {
      throw new TectonClientException(
          String.format(TectonErrorMessage.INVALID_GET_FEATURE_REQUEST, e.getMessage()));
    }
  }
}
