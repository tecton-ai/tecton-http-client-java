package com.tecton.client.request;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.tecton.client.exceptions.TectonClientException;
import com.tecton.client.exceptions.TectonErrorMessage;
import com.tecton.client.transport.TectonHttpClient.HttpMethod;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

/**
 * A subclass of {@link com.tecton.client.request.AbstractTectonRequest} that represents a request
 * to the <i>/get-features</i> endpoint to retrieve feature values from Tecton's online store
 */
public class GetFeaturesRequest extends AbstractTectonRequest {
  private static final String ENDPOINT = "/api/v1/feature-service/get-features";
  private static final HttpMethod httpMethod = HttpMethod.POST;
  // Always include name and data_type in metadata options
  private static final Set<MetadataOption> defaultMetadataOptions =
      EnumSet.of(MetadataOption.NAME, MetadataOption.DATA_TYPE);

  private final JsonAdapter<GetFeaturesRequestJson> jsonAdapter;
  private final GetFeaturesRequestData getFeaturesRequestData;
  private final Set<MetadataOption> metadataOptions;

  /**
   * Constructor that creates a new GetFeaturesRequest with specified parameters and default
   * MetadataOptions.
   *
   * <p>Note: Each GetFeaturesRequest will always include the MetadataOption.NAME and
   * MetadataOption.DATA_TYPE options
   *
   * @param workspaceName Name of the workspace in which the Feature Service is defined
   * @param featureServiceName Name of the Feature Service for which the feature vector is being
   *     requested
   * @param getFeaturesRequestData {@link com.tecton.client.request.GetFeaturesRequestData} object
   *     with joinKeyMap and/or requestContextMap
   */
  public GetFeaturesRequest(
      String workspaceName,
      String featureServiceName,
      GetFeaturesRequestData getFeaturesRequestData) {
    super(ENDPOINT, httpMethod, workspaceName, featureServiceName);
    validateRequestParameters(getFeaturesRequestData);
    this.getFeaturesRequestData = getFeaturesRequestData;
    this.metadataOptions = defaultMetadataOptions;
    Moshi moshi = new Moshi.Builder().build();
    jsonAdapter = moshi.adapter(GetFeaturesRequestJson.class);
  }

  /**
   * Constructor that creates a new GetFeaturesRequest with the specified parameters
   *
   * @param workspaceName Name of the workspace in which the Feature Service is defined
   * @param featureServiceName Name of the Feature Service for which the feature vector is being
   *     requested
   * @param getFeaturesRequestData {@link com.tecton.client.request.GetFeaturesRequestData} object
   *     with joinKeyMap and/or requestContextMap
   * @param metadataOptions Options for retrieving additional metadata about the feature values.
   *     Note if MetadataOption.ALL is included, all metadata will be requested. If
   *     MetadataOption.NONE is included, all other arguments will be ignored. By default,
   *     MetadataOption.NAME and MetadataOption.DATA_TYPE will be added to each request
   */
  public GetFeaturesRequest(
      String workspaceName,
      String featureServiceName,
      GetFeaturesRequestData getFeaturesRequestData,
      MetadataOption... metadataOptions) {

    super(ENDPOINT, httpMethod, workspaceName, featureServiceName);
    validateRequestParameters(getFeaturesRequestData);
    this.getFeaturesRequestData = getFeaturesRequestData;

    List<MetadataOption> metadataOptionList = Arrays.asList(metadataOptions);
    if (metadataOptionList.contains(MetadataOption.ALL)) {
      // Add everything except ALL and NONE from MetadataOption EnumSet
      this.metadataOptions =
          EnumSet.complementOf(EnumSet.of(MetadataOption.ALL, MetadataOption.NONE));
    } else if (metadataOptionList.contains(MetadataOption.NONE)) {
      this.metadataOptions = EnumSet.noneOf(MetadataOption.class);
    } else {
      this.metadataOptions = EnumSet.copyOf(metadataOptionList);
    }
    this.metadataOptions.addAll(defaultMetadataOptions); // default metadata options
    Moshi moshi = new Moshi.Builder().build();
    jsonAdapter = moshi.adapter(GetFeaturesRequestJson.class);
  }

  GetFeaturesRequestData getFeaturesRequestData() {
    return this.getFeaturesRequestData;
  }

  Set<MetadataOption> getMetadataOptions() {
    return this.metadataOptions;
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
    Map<String, String> join_key_map;
    Map<String, Object> request_context_map;
    Map<String, Boolean> metadata_options;
  }

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
    GetFeaturesRequestJson getFeaturesRequestJson = new GetFeaturesRequestJson(getFeaturesFields);
    try {
      return jsonAdapter.toJson(getFeaturesRequestJson);
    } catch (Exception e) {
      throw new TectonClientException(
          String.format(TectonErrorMessage.INVALID_GET_FEATURE_REQUEST, e.getMessage()));
    }
  }

  /**
   * Enum representing options for different metadata information that can be requested from the
   * FeatureService API
   */
  public enum MetadataOption {
    /**
     * Include feature name in the response. By default, this is always included in a
     * GetFeaturesRequest
     */
    NAME("include_names"),
    /** Include feature effective_time in the response */
    EFFECTIVE_TIME("include_effective_times"),
    /**
     * Include feature data_type in the response. By default, this is always included in a
     * GetFeaturesRequest
     */
    DATA_TYPE("include_data_types"),
    /** Include SLO Info in the response */
    SLO_INFO("include_slo_info"),
    /** Include all metadata in the response */
    ALL(),
    /**
     * Include no metadata in the response. Note that the default metadata options - NAME and
     * DATA_TYPE will still be included
     */
    NONE();

    private final String jsonName;

    MetadataOption() {
      this.jsonName = StringUtils.EMPTY;
    }

    MetadataOption(String name) {
      this.jsonName = name;
    }

    public String getJsonName() {
      return jsonName;
    }
  }

  private void validateRequestParameters(GetFeaturesRequestData getFeaturesRequestData) {
    if (getFeaturesRequestData.isEmptyJoinKeyMap()
        && getFeaturesRequestData.isEmptyRequestContextMap()) {
      throw new TectonClientException(TectonErrorMessage.EMPTY_REQUEST_MAPS);
    }
  }
}
