package ai.tecton.client.request;

import ai.tecton.client.exceptions.InvalidRequestParameterException;
import ai.tecton.client.exceptions.TectonErrorMessage;
import ai.tecton.client.transport.TectonHttpClient;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

/**
 * A subclass of {@link AbstractTectonRequest} that represents a request to the
 * <i>feature-service/metadata</i> endpoint to retrieve metadata about a FeatureService, including
 * the schema for join keys and request context, and the in-order output schema of returned feature
 * values.
 */
public class GetFeatureServiceMetadataRequest extends AbstractTectonRequest {

  private static final TectonHttpClient.HttpMethod method = TectonHttpClient.HttpMethod.POST;
  private static final String ENDPOINT = "/api/v1/feature-service/metadata";
  private static final String DEFAULT_WORKSPACE = "prod";
  private final JsonAdapter<GetFeatureServiceMetadataJson> jsonAdapter;

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

  /**
   * Get the JSON representation of the request that will be sent to the /metadata endpoint.
   *
   * @return JSON String representation of {@link GetFeatureServiceMetadataRequest}
   */
  @Override
  public String requestToJson() {
    GetFeatureServiceMetadataFields serviceMetadataFields = new GetFeatureServiceMetadataFields();
    serviceMetadataFields.feature_service_name = super.getFeatureServiceName();
    serviceMetadataFields.workspace_name = super.getWorkspaceName();

    try {
      return jsonAdapter.toJson(new GetFeatureServiceMetadataJson(serviceMetadataFields));
    } catch (Exception e) {
      throw new InvalidRequestParameterException(
          String.format(TectonErrorMessage.INVALID_GET_SERVICE_METADATA_REQUEST, e.getMessage()));
    }
  }

  /**
   * A Builder class for building instances of {@link GetFeatureServiceMetadataRequest} objects from
   * values configured by setters
   */
  public static final class Builder {
    private String workspaceName;
    private String featureServiceName;

    /**
     * Setter for workspaceName
     *
     * @param workspaceName Name of the workspace to fetch metadata for the FeatureService from
     * @return this Builder
     */
    public Builder workspaceName(String workspaceName) {
      this.workspaceName = workspaceName;
      return this;
    }

    /**
     * Setter for featureServiceName
     *
     * @param featureServiceName Name of the Feature Service for which the metadata is being
     *     requested
     * @return this Builder
     */
    public Builder featureServiceName(String featureServiceName) {
      this.featureServiceName = featureServiceName;
      return this;
    }

    /**
     * Returns an instance of {@link GetFeatureServiceMetadataRequest}
     *
     * @return {@link GetFeatureServiceMetadataRequest} object
     * @throws InvalidRequestParameterException when workspaceName and/or featureServiceName is null
     *     or empty
     */
    public GetFeatureServiceMetadataRequest build() {
      return new GetFeatureServiceMetadataRequest(featureServiceName, workspaceName);
    }
  }
}
