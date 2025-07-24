package ai.tecton.client.request;

import ai.tecton.client.exceptions.InvalidRequestParameterException;
import ai.tecton.client.exceptions.TectonErrorMessage;
import ai.tecton.client.model.MetadataOption;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A subclass of {@link AbstractTectonRequest} that represents a request to the <i>/get-features</i>
 * endpoint to retrieve feature values from Tecton's online store
 */
public class GetFeaturesRequest extends AbstractGetFeaturesRequest {

  static final String ENDPOINT = "/api/v1/feature-service/get-features";
  private final JsonAdapter<GetFeaturesRequestJson> jsonAdapter;
  private final GetFeaturesRequestData getFeaturesRequestData;
  private final RequestOptions requestOptions;
  private final Moshi moshi = new Moshi.Builder().add(SerializeNulls.JSON_ADAPTER_FACTORY).build();

  /**
   * Constructor that creates a new GetFeaturesRequest with specified parameters. {@code
   * metadataOptions} will default to {@link RequestConstants#DEFAULT_METADATA_OPTIONS}
   *
   * @param workspaceName Name of the workspace in which the Feature Service is defined
   * @param featureServiceName Name of the Feature Service for which the feature vector is being
   *     requested
   * @param getFeaturesRequestData {@link GetFeaturesRequestData} object with joinKeyMap and/or
   *     requestContextMap
   */
  public GetFeaturesRequest(
      String workspaceName,
      String featureServiceName,
      GetFeaturesRequestData getFeaturesRequestData) {

    super(workspaceName, featureServiceName, ENDPOINT, RequestConstants.DEFAULT_METADATA_OPTIONS);
    validateRequestParameters(getFeaturesRequestData);
    this.getFeaturesRequestData = getFeaturesRequestData;
    this.requestOptions = null;
    jsonAdapter = moshi.adapter(GetFeaturesRequestJson.class);
  }

  /**
   * Constructor that creates a new GetFeaturesRequest with the specified parameters
   *
   * @param workspaceName Name of the workspace in which the Feature Service is defined
   * @param featureServiceName Name of the Feature Service for which the feature vector is being
   *     requested
   * @param getFeaturesRequestData {@link GetFeaturesRequestData} object with joinKeyMap and/or
   *     requestContextMap
   * @param metadataOptions A {@link Set} of {@link MetadataOption} for retrieving additional
   *     metadata about the feature values. Use {@link RequestConstants#ALL_METADATA_OPTIONS} to
   *     request all metadata and {@link RequestConstants#NONE_METADATA_OPTIONS} to request no
   *     metadata respectively. By default, {@link RequestConstants#DEFAULT_METADATA_OPTIONS} will
   *     be added to each request
   */
  public GetFeaturesRequest(
      String workspaceName,
      String featureServiceName,
      GetFeaturesRequestData getFeaturesRequestData,
      Set<MetadataOption> metadataOptions) {

    super(workspaceName, featureServiceName, ENDPOINT, metadataOptions);
    validateRequestParameters(getFeaturesRequestData);
    this.getFeaturesRequestData = getFeaturesRequestData;
    this.requestOptions = null;
    jsonAdapter = moshi.adapter(GetFeaturesRequestJson.class);
  }

  /**
   * Constructor that creates a new GetFeaturesRequest with the specified parameters including
   * requestOptions
   *
   * @param workspaceName Name of the workspace in which the Feature Service is defined
   * @param featureServiceName Name of the Feature Service for which the feature vector is being
   *     requested
   * @param getFeaturesRequestData {@link GetFeaturesRequestData} object with joinKeyMap and/or
   *     requestContextMap
   * @param metadataOptions A {@link Set} of {@link MetadataOption} for retrieving additional
   *     metadata about the feature values. Use {@link RequestConstants#ALL_METADATA_OPTIONS} to
   *     request all metadata and {@link RequestConstants#NONE_METADATA_OPTIONS} to request no
   *     metadata respectively. By default, {@link RequestConstants#DEFAULT_METADATA_OPTIONS} will
   *     be added to each request
   * @param requestOptions {@link RequestOptions} object with request-level options to control
   *     feature server behavior
   */
  public GetFeaturesRequest(
      String workspaceName,
      String featureServiceName,
      GetFeaturesRequestData getFeaturesRequestData,
      Set<MetadataOption> metadataOptions,
      RequestOptions requestOptions) {

    super(workspaceName, featureServiceName, ENDPOINT, metadataOptions);
    validateRequestParameters(getFeaturesRequestData);
    this.getFeaturesRequestData = getFeaturesRequestData;
    this.requestOptions = requestOptions;
    jsonAdapter = moshi.adapter(GetFeaturesRequestJson.class);
  }

  GetFeaturesRequestData getFeaturesRequestData() {
    return this.getFeaturesRequestData;
  }

  RequestOptions getRequestOptions() {
    return this.requestOptions;
  }

  static class GetFeaturesRequestJson {
    GetFeaturesFields params;

    GetFeaturesRequestJson(GetFeaturesFields params) {
      this.params = params;
    }
  }

  static class GetFeaturesFields {
    String feature_service_name;
    String workspace_name;
    @SerializeNulls Map<String, String> join_key_map;
    @SerializeNulls Map<String, Object> request_context_map;
    Map<String, Boolean> metadata_options;
    Map<String, Object> request_options;
  }

  /**
   * Get the JSON representation of the request that will be sent to the /get-features endpoint.
   *
   * @return JSON String representation of {@link GetFeaturesRequest}
   */
  @Override
  public String requestToJson() {
    GetFeaturesFields getFeaturesFields = new GetFeaturesFields();
    getFeaturesFields.feature_service_name = this.getFeatureServiceName();
    getFeaturesFields.workspace_name = this.getWorkspaceName();
    if (!getFeaturesRequestData().isEmptyJoinKeyMap()) {
      getFeaturesFields.join_key_map = getFeaturesRequestData().getJoinKeyMap();
    }
    if (!getFeaturesRequestData().isEmptyRequestContextMap()) {
      getFeaturesFields.request_context_map = getFeaturesRequestData().getRequestContextMap();
    }
    if (!metadataOptions.isEmpty()) {
      getFeaturesFields.metadata_options =
          metadataOptions.stream()
              .collect(Collectors.toMap(MetadataOption::getJsonName, (a) -> Boolean.TRUE));
    }
    if (requestOptions != null && !requestOptions.isEmpty()) {
      getFeaturesFields.request_options = requestOptions.getOptions();
    }
    GetFeaturesRequestJson getFeaturesRequestJson = new GetFeaturesRequestJson(getFeaturesFields);
    try {
      return jsonAdapter.toJson(getFeaturesRequestJson);
    } catch (Exception e) {
      throw new InvalidRequestParameterException(
          String.format(TectonErrorMessage.INVALID_GET_FEATURE_REQUEST, e.getMessage()));
    }
  }

  /** Overrides <i>equals()</i> in class {@link Object} */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    GetFeaturesRequest that = (GetFeaturesRequest) o;
    return getFeaturesRequestData.equals(that.getFeaturesRequestData)
        && Objects.equals(requestOptions, that.requestOptions);
  }

  /** Overrides <i>hashCode()</i> in class {@link Object} */
  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), getFeaturesRequestData, requestOptions);
  }

  /**
   * A Builder class for building instances of {@link GetFeaturesRequest} objects from values
   * configured by setters
   */
  public static final class Builder {
    Set<MetadataOption> metadataOptions;
    private String workspaceName;
    private String featureServiceName;
    private GetFeaturesRequestData getFeaturesRequestData;
    private RequestOptions requestOptions;

    /** Constructor for instantiating an empty Builder */
    public Builder() {
      this.metadataOptions = new HashSet<>();
    }

    /**
     * Setter for metadataOptions
     *
     * @param metadataOptions A {@link Set} of {@link MetadataOption} for retrieving additional
     *     metadata about the feature values. Use {@link RequestConstants#ALL_METADATA_OPTIONS} to
     *     request all metadata and {@link RequestConstants#NONE_METADATA_OPTIONS} to request no
     *     metadata respectively. By default, {@link RequestConstants#DEFAULT_METADATA_OPTIONS} will
     *     be added to each request
     * @return this Builder
     */
    public Builder metadataOptions(Set<MetadataOption> metadataOptions) {
      this.metadataOptions = metadataOptions;
      return this;
    }

    /**
     * Setter for workspaceName
     *
     * @param workspaceName Name of the workspace in which the Feature Service is defined
     * @return this Builder
     */
    public Builder workspaceName(String workspaceName) {
      this.workspaceName = workspaceName;
      return this;
    }

    /**
     * Setter for featureServiceName
     *
     * @param featureServiceName Name of the Feature Service for which the feature vector is being
     *     requested
     * @return this Builder
     */
    public Builder featureServiceName(String featureServiceName) {
      this.featureServiceName = featureServiceName;
      return this;
    }

    /**
     * Setter for {@link GetFeaturesRequestData}
     *
     * @param getFeaturesRequestData {@link GetFeaturesRequestData} object with joinKeyMap and/or
     *     requestContextMap
     * @return this Builder
     */
    public Builder getFeaturesRequestData(GetFeaturesRequestData getFeaturesRequestData) {
      this.getFeaturesRequestData = getFeaturesRequestData;
      return this;
    }

    /**
     * Setter for {@link RequestOptions}
     *
     * @param requestOptions {@link RequestOptions} object with request-level options to control
     *     feature server behavior
     * @return this Builder
     */
    public Builder requestOptions(RequestOptions requestOptions) {
      this.requestOptions = requestOptions;
      return this;
    }

    /**
     * Returns an instance of {@link GetFeaturesRequest} created from the fields set on this builder
     *
     * @return {@link GetFeaturesRequest} object
     * @throws InvalidRequestParameterException when workspaceName and/or featureServiceName is null
     *     or empty
     */
    public GetFeaturesRequest build() {
      if (this.requestOptions != null) {
        // Handle the case where requestOptions is set and metadataOptions is or is not set (using a
        // default if not set).
        Set<MetadataOption> options =
            this.metadataOptions.isEmpty()
                ? RequestConstants.DEFAULT_METADATA_OPTIONS
                : this.metadataOptions;
        return new GetFeaturesRequest(
            workspaceName, featureServiceName, getFeaturesRequestData, options, requestOptions);
      } else if (this.metadataOptions.isEmpty()) {
        // Handle the case where neither requestOptions nor metadataOptions are set.
        return new GetFeaturesRequest(workspaceName, featureServiceName, getFeaturesRequestData);
      } else {
        // Handle the case where metadataOptions is set and requestOptions is not set.
        return new GetFeaturesRequest(workspaceName, featureServiceName, getFeaturesRequestData);
    }
  }
}
