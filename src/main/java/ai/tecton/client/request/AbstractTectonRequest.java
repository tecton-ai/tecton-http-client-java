package ai.tecton.client.request;

import ai.tecton.client.exceptions.TectonClientException;
import ai.tecton.client.exceptions.TectonErrorMessage;
import ai.tecton.client.transport.TectonHttpClient;
import java.util.Objects;
import org.apache.commons.lang3.Validate;

/** An abstract parent class for Tecton FeatureService API Request subclasses */
public abstract class AbstractTectonRequest {

  private final String endpoint;
  private final TectonHttpClient.HttpMethod method;
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
      String endpoint,
      TectonHttpClient.HttpMethod method,
      String workspaceName,
      String featureServiceName)
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
  public TectonHttpClient.HttpMethod getMethod() {
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

  static void validateRequestParameters(String workspaceName, String featureServiceName) {
    try {
      Validate.notEmpty(workspaceName, TectonErrorMessage.INVALID_WORKSPACENAME);
      Validate.notEmpty(featureServiceName, TectonErrorMessage.INVALID_FEATURESERVICENAME);
    } catch (Exception e) {
      throw new TectonClientException(e.getMessage());
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AbstractTectonRequest that = (AbstractTectonRequest) o;
    return endpoint.equals(that.endpoint)
        && method == that.method
        && workspaceName.equals(that.workspaceName)
        && featureServiceName.equals(that.featureServiceName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(endpoint, method, workspaceName, featureServiceName);
  }
}
