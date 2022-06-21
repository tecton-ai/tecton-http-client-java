package com.tecton.client.request;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.tecton.client.exceptions.TectonClientException;
import com.tecton.client.exceptions.TectonErrorMessage;
import com.tecton.client.model.GetFeaturesRequestData;
import com.tecton.client.transport.TectonHttpClient.HttpMethod;
import org.apache.commons.lang3.Validate;

import java.util.Map;

public class GetFeaturesRequest extends AbstractTectonRequest {
  private static final String ENDPOINT = "/api/v1/feature-service/get-features";
  private static final HttpMethod httpMethod = HttpMethod.POST;

  private GetFeaturesRequestData getFeaturesRequestData;

  public GetFeaturesRequest(
      String workspaceName,
      String featureServiceName,
      GetFeaturesRequestData getFeaturesRequestData) {
    super(ENDPOINT, httpMethod, workspaceName, featureServiceName);
    validateRequestParameters(workspaceName, featureServiceName, getFeaturesRequestData);
    this.getFeaturesRequestData = getFeaturesRequestData;
  }

  public GetFeaturesRequestData getFeaturesRequestData() {
    return this.getFeaturesRequestData;
  }

  public String requestToJson() {
    Moshi moshi = new Moshi.Builder().build();
    JsonAdapter<GetFeatureRequestJson> jsonAdapter = moshi.adapter(GetFeatureRequestJson.class);

    GetFeatureRequestJson getFeaturesRequestJson = new GetFeatureRequestJson();
    getFeaturesRequestJson.feature_service_name = this.getFeatureServiceName();
    getFeaturesRequestJson.workspace_name = this.getWorkspaceName();
    if (!getFeaturesRequestData().isEmptyJoinKeyMap()) {
      getFeaturesRequestJson.join_key_map = getFeaturesRequestData().getJoinKeyMap();
    }
    if (!getFeaturesRequestData().isEmptyRequestContextMap()) {
      getFeaturesRequestJson.request_context_map = getFeaturesRequestData().getRequestContextMap();
    }
    try {
      return jsonAdapter.toJson(getFeaturesRequestJson);
    } catch (Exception e) {
      throw new TectonClientException(TectonErrorMessage.INVALID_GET_FEATURE_REQUEST);
    }
  }

  public static class GetFeatureRequestJson {
    String feature_service_name;
    String workspace_name;
    Map<String, String> join_key_map;
    Map<String, Object> request_context_map;
  }

  private void validateRequestParameters(
      String workspaceName,
      String featureServiceName,
      GetFeaturesRequestData getFeaturesRequestData) {

    Validate.notEmpty(workspaceName, TectonErrorMessage.INVALID_WORKSPACENAME);
    Validate.notEmpty(featureServiceName, TectonErrorMessage.INVALID_FEATURESERVICENAME);

    if (getFeaturesRequestData.isEmptyJoinKeyMap()
        && getFeaturesRequestData.isEmptyRequestContextMap()) {
      throw new TectonClientException(TectonErrorMessage.EMPTY_REQUEST_MAPS);
    }
  }
}
