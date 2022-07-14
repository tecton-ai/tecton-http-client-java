package com.tecton.client.request;

import com.tecton.client.exceptions.TectonClientException;
import com.tecton.client.exceptions.TectonErrorMessage;
import com.tecton.client.transport.TectonHttpClient.HttpMethod;
import org.apache.commons.lang3.Validate;

/** An abstract parent class for Tecton FeatureService API Request subclasses */
public abstract class AbstractTectonRequest {

  private final String endpoint;
  private final HttpMethod method;
  private final String workspaceName;
  private final String featureServiceName;

  /**
   * Parent class constructor that configures the request endpoint, request method, workspaceName
   * and featureServiceName
   *
   * @param endpoint
   * @param method
   * @param workspaceName
   * @param featureServiceName
   * @throws TectonClientException when workspaceName or featureServiceName is null or empty
   */
  public AbstractTectonRequest(
      String endpoint, HttpMethod method, String workspaceName, String featureServiceName)
      throws TectonClientException {
    validateRequestParameters(workspaceName, featureServiceName);
    this.endpoint = endpoint;
    this.method = method;
    this.workspaceName = workspaceName;
    this.featureServiceName = featureServiceName;
  }

  /**
   * Returns the endpoint for request type. This endpoint will be appended to the base URL provided
   * to the TectonClient
   */
  public String getEndpoint() {
    return endpoint;
  }

  /** Returns the Http Method used by the request type. */
  public HttpMethod getMethod() {
    return method;
  }

  /** Returns the featureServiceName set for the request */
  public String getFeatureServiceName() {
    return this.featureServiceName;
  }

  /** Returns the workspaceName set for the request */
  public String getWorkspaceName() {
    return this.workspaceName;
  }

  public abstract String requestToJson();

  private void validateRequestParameters(String workspaceName, String featureServiceName) {
    Validate.notEmpty(workspaceName, TectonErrorMessage.INVALID_WORKSPACENAME);
    Validate.notEmpty(featureServiceName, TectonErrorMessage.INVALID_FEATURESERVICENAME);
  }
}
