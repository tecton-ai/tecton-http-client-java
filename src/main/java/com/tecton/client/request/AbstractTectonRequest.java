package com.tecton.client.request;

import com.tecton.client.exceptions.TectonErrorMessage;
import com.tecton.client.transport.TectonHttpClient.HttpMethod;
import org.apache.commons.lang3.Validate;

public abstract class AbstractTectonRequest {

  private final String endpoint;
  private final HttpMethod method;
  private final String workspaceName;
  private final String featureServiceName;

  public AbstractTectonRequest(
      String endpoint, HttpMethod method, String workspaceName, String featureServiceName) {
    validateRequestParameters(workspaceName, featureServiceName);
    this.endpoint = endpoint;
    this.method = method;
    this.workspaceName = workspaceName;
    this.featureServiceName = featureServiceName;
  }

  public String getEndpoint() {
    return endpoint;
  }

  public HttpMethod getMethod() {
    return method;
  }

  public String getFeatureServiceName() {
    return this.featureServiceName;
  }

  public String getWorkspaceName() {
    return this.workspaceName;
  }

  public abstract String requestToJson();

  private void validateRequestParameters(String workspaceName, String featureServiceName) {
    Validate.notEmpty(workspaceName, TectonErrorMessage.INVALID_WORKSPACENAME);
    Validate.notEmpty(featureServiceName, TectonErrorMessage.INVALID_FEATURESERVICENAME);
  }
}
