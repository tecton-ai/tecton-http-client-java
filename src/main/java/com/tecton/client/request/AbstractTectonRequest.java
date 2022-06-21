package com.tecton.client.request;

import com.squareup.moshi.Json;
import com.tecton.client.transport.TectonHttpClient.HttpMethod;

public abstract class AbstractTectonRequest {

  private final transient String endpoint;
  private final transient HttpMethod method;

  @Json(name = "workspace_name")
  private String workspaceName;

  @Json(name = "feature_service_name")
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

  public String getWorkspaceName() {
    return workspaceName;
  }

  public String getFeatureServiceName() {
    return featureServiceName;
  }
}
