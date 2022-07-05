package com.tecton.client.request;

import com.tecton.client.transport.TectonHttpClient.HttpMethod;

public abstract class AbstractTectonRequest {

  private final String endpoint;
  private final HttpMethod method;
  private String workspaceName;
  private String featureServiceName;


  public AbstractTectonRequest(
      String endpoint, HttpMethod method, String workspaceName, String featureServiceName) {
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

  abstract public String requestToJson();

}
