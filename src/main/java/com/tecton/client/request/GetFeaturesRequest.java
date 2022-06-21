package com.tecton.client.request;

import com.tecton.client.model.GetFeaturesRequestData;
import com.tecton.client.transport.TectonHttpClient;

import java.util.EnumSet;

public class GetFeaturesRequest extends AbstractTectonRequest {
  private static final String ENDPOINT = "/api/v1/feature-service/get-features";
  GetFeaturesRequestData featureRequestData;
  EnumSet<MetadataOption> metadataOptions;

  public GetFeaturesRequest(
      String workspaceName, String featureServiceName, GetFeaturesRequestData featureRequestData) {
    super(ENDPOINT, TectonHttpClient.HttpMethod.POST, workspaceName, featureServiceName);
    this.featureRequestData = featureRequestData;
  }

  enum MetadataOption {
    NAME,
    EFFECTIVE_TIME,
    DATA_TYPE,
    SLO_INFO,
    ALL,
    NONE;
  }
}
