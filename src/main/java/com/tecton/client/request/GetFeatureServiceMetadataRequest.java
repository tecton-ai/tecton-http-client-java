package com.tecton.client.request;

import com.tecton.client.transport.TectonHttpClient.HttpMethod;

public class GetFeatureServiceMetadataRequest extends AbstractTectonRequest {

  private static final HttpMethod method = HttpMethod.POST;
  private static final String ENDPOINT = "/api/v1/feature-service/metadata";

  public GetFeatureServiceMetadataRequest(String workspaceName, String featureServiceName) {
    super(ENDPOINT, method, workspaceName, featureServiceName);
  }

  @Override
  public String requestToJson() {
    return null;
  }
}
