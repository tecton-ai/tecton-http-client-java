package com.tecton.client.request;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.tecton.client.exceptions.TectonClientException;
import com.tecton.client.exceptions.TectonErrorMessage;
import com.tecton.client.transport.TectonHttpClient.HttpMethod;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.util.*;
import java.util.stream.Collectors;

public class GetFeaturesRequest extends AbstractTectonRequest {
  private static final String ENDPOINT = "/api/v1/feature-service/get-features";
  private static final HttpMethod httpMethod = HttpMethod.POST;

  private JsonAdapter<GetFeaturesRequestJson> jsonAdapter;
  private final GetFeaturesRequestData getFeaturesRequestData;
  private final Set<MetadataOption> metadataOptions;

  public GetFeaturesRequest(
      String workspaceName,
      String featureServiceName,
      GetFeaturesRequestData getFeaturesRequestData) {
    super(ENDPOINT, httpMethod, workspaceName, featureServiceName);
    validateRequestParameters(workspaceName, featureServiceName, getFeaturesRequestData);
    this.getFeaturesRequestData = getFeaturesRequestData;
    this.metadataOptions = EnumSet.noneOf(MetadataOption.class);
    Moshi moshi = new Moshi.Builder().build();
    jsonAdapter = moshi.adapter(GetFeaturesRequestJson.class);
  }

  public GetFeaturesRequest(
      String workspaceName,
      String featureServiceName,
      GetFeaturesRequestData getFeaturesRequestData,
      MetadataOption... metadataOptions) {

    super(ENDPOINT, httpMethod, workspaceName, featureServiceName);
    validateRequestParameters(workspaceName, featureServiceName, getFeaturesRequestData);
    this.getFeaturesRequestData = getFeaturesRequestData;

    List<MetadataOption> metadataOptionList = Arrays.asList(metadataOptions);
    if (metadataOptionList.contains(MetadataOption.ALL)) {
      this.metadataOptions =
          EnumSet.complementOf(EnumSet.of(MetadataOption.ALL, MetadataOption.NONE));
    } else if (metadataOptionList.contains(MetadataOption.NONE)) {
      this.metadataOptions = EnumSet.noneOf(MetadataOption.class);
    } else {
      this.metadataOptions = EnumSet.copyOf(metadataOptionList);
    }
    Moshi moshi = new Moshi.Builder().build();
    jsonAdapter = moshi.adapter(GetFeaturesRequestJson.class);
  }

  public GetFeaturesRequestData getFeaturesRequestData() {
    return this.getFeaturesRequestData;
  }

  public Set<MetadataOption> getMetadataOptions() {
    return this.metadataOptions;
  }

  private static class GetFeaturesRequestJson {
    String feature_service_name;
    String workspace_name;
    Map<String, String> join_key_map;
    Map<String, Object> request_context_map;
    Map<String, Boolean> metadata_options;
  }

  String requestToJson() {
    GetFeaturesRequestJson getFeaturesRequestJson = new GetFeaturesRequestJson();
    getFeaturesRequestJson.feature_service_name = this.getFeatureServiceName();
    getFeaturesRequestJson.workspace_name = this.getWorkspaceName();
    if (!getFeaturesRequestData().isEmptyJoinKeyMap()) {
      getFeaturesRequestJson.join_key_map = getFeaturesRequestData().getJoinKeyMap();
    }
    if (!getFeaturesRequestData().isEmptyRequestContextMap()) {
      getFeaturesRequestJson.request_context_map = getFeaturesRequestData().getRequestContextMap();
    }
    if (!metadataOptions.isEmpty()) {
      getFeaturesRequestJson.metadata_options =
          metadataOptions.stream()
              .collect(Collectors.toMap(MetadataOption::getJsonName, (a) -> Boolean.TRUE));
    }
    try {
      return jsonAdapter.toJson(getFeaturesRequestJson);
    } catch (Exception e) {
      throw new TectonClientException(TectonErrorMessage.INVALID_GET_FEATURE_REQUEST);
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

  private void validateRequestParameters(
      String workspaceName,
      String featureServiceName,
      GetFeaturesRequestData getFeaturesRequestData) {

    Validate.notEmpty(workspaceName, TectonErrorMessage.INVALID_WORKSPACENAME);
    Validate.notEmpty(featureServiceName, TectonErrorMessage.INVALID_FEATURESERVICENAME);

    if (getFeaturesRequestData.isEmptyJoinKeyMap()
        && getFeaturesRequestData.isEmptyRequestContextMap()) {
      throw new TectonClientException(TectonErrorMessage.EMPTY_REQUEST_MAPS);
    }
  }
}