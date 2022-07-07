package com.tecton.client.request;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.tecton.client.exceptions.TectonClientException;
import com.tecton.client.exceptions.TectonErrorMessage;
import com.tecton.client.transport.TectonHttpClient.HttpMethod;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class GetFeaturesRequest extends AbstractTectonRequest {
  private static final String ENDPOINT = "/api/v1/feature-service/get-features";
  private static final HttpMethod httpMethod = HttpMethod.POST;
  // Always include name and data_type in metadata options
  private static final Set<MetadataOption> defaultMetadataOptions =
      EnumSet.of(MetadataOption.NAME, MetadataOption.DATA_TYPE);

  private JsonAdapter<GetFeaturesRequestJson> jsonAdapter;
  private final GetFeaturesRequestData getFeaturesRequestData;
  private Set<MetadataOption> metadataOptions;

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

  public enum MetadataOption {
    NAME("include_names"),
    EFFECTIVE_TIME("include_effective_times"),
    DATA_TYPE("include_data_types"),
    SLO_INFO("include_slo_info"),
    ALL(),
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
