package com.tecton.client.request;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.tecton.client.exceptions.TectonClientException;
import com.tecton.client.exceptions.TectonErrorMessage;
import com.tecton.client.transport.TectonHttpClient.HttpMethod;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

public class GetFeaturesRequest extends AbstractTectonRequest {
  private static final String ENDPOINT = "/api/v1/feature-service/get-features";
  private static final HttpMethod httpMethod = HttpMethod.POST;
  // Always include name and data_type in metadata options
  private static final Set<MetadataOption> defaultMetadataOptions =
      EnumSet.of(MetadataOption.NAME, MetadataOption.DATA_TYPE);

  private JsonAdapter<GetFeaturesRequestJson> jsonAdapter;
  private Map<String, String> joinKeyMap;
  private Map<String, Object> requestContextMap;
  private Set<MetadataOption> metadataOptions;

  GetFeaturesRequest(String workspaceName, String featureServiceName) {
    super(ENDPOINT, httpMethod, workspaceName, featureServiceName);
    this.joinKeyMap = new HashMap<>();
    this.requestContextMap = new HashMap<>();
    this.metadataOptions = defaultMetadataOptions;
    Moshi moshi = new Moshi.Builder().build();
    jsonAdapter = moshi.adapter(GetFeaturesRequestJson.class);
  }

  Set<MetadataOption> getMetadataOptions() {
    return this.metadataOptions;
  }

  void setJoinKeyMap(Map<String, String> joinKeyMap) {
    this.joinKeyMap = joinKeyMap;
  }

  void setRequestContextMap(Map<String, Object> requestContextMap) {
    this.requestContextMap = requestContextMap;
  }

  void setMetadataOptions(Set<MetadataOption> metadataOptions) {
    this.metadataOptions = metadataOptions;
  }

  Map<String, String> getJoinKeyMap() {
    return this.joinKeyMap;
  }

  Map<String, Object> getRequestContextMap() {
    return this.requestContextMap;
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
    if (!this.joinKeyMap.isEmpty()) {
      getFeaturesFields.join_key_map = joinKeyMap;
    }
    if (!this.requestContextMap.isEmpty()) {
      getFeaturesFields.request_context_map = requestContextMap;
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

  public static class Builder {
    private String workspaceName;
    private String featureServiceName;
    private Map<String, String> joinKeyMap;
    private Map<String, Object> requestContextMap;
    List<MetadataOption> metadataOptionList;

    public Builder() {
      this.joinKeyMap = new HashMap<>();
      this.requestContextMap = new HashMap<>();
      this.metadataOptionList = new ArrayList<>();
    }

    public Builder workspaceName(String workspaceName) {
      this.workspaceName = workspaceName;
      return this;
    }

    public Builder featureServiceName(String featureServiceName) {
      this.featureServiceName = featureServiceName;
      return this;
    }

    public Builder joinKeyMap(Map<String, String> joinKeyMap) {
      this.joinKeyMap = joinKeyMap;
      return this;
    }

    public Builder requestContextMap(Map<String, Object> requestContextMap) {
      this.requestContextMap = requestContextMap;
      return this;
    }

    public Builder joinKey(String key, String value) {
      joinKeyMap.put(key, value);
      return this;
    }

    public Builder joinKey(String key, Long value) {
      joinKeyMap.put(key, value.toString());
      return this;
    }

    public Builder requestContext(String key, String value) {
      requestContextMap.put(key, value);
      return this;
    }

    public Builder requestContext(String key, Long value) {
      requestContextMap.put(key, value.toString());
      return this;
    }

    public Builder requestContext(String key, Double value) {
      requestContextMap.put(key, value);
      return this;
    }

    public Builder metadataOptions(MetadataOption... metadataOptions) {
      this.metadataOptionList = Arrays.asList(metadataOptions);
      return this;
    }

    public GetFeaturesRequest build() throws TectonClientException {

      GetFeaturesRequest getFeaturesRequest =
          new GetFeaturesRequest(workspaceName, featureServiceName);
      validateRequestParameters();
      if (!this.joinKeyMap.isEmpty()) {
        getFeaturesRequest.setJoinKeyMap(joinKeyMap);
      }
      if (!this.requestContextMap.isEmpty()) {
        getFeaturesRequest.setRequestContextMap(requestContextMap);
      }

      if (this.metadataOptionList.isEmpty()) {
        getFeaturesRequest.setMetadataOptions(defaultMetadataOptions);
      } else {
        Set<MetadataOption> metadataOptions;
        if (this.metadataOptionList.contains(MetadataOption.ALL)) {
          // Add everything except ALL and NONE from MetadataOption EnumSet
          metadataOptions =
              EnumSet.complementOf(EnumSet.of(MetadataOption.ALL, MetadataOption.NONE));
        } else if (metadataOptionList.contains(MetadataOption.NONE)) {
          metadataOptions = EnumSet.noneOf(MetadataOption.class);
        } else {
          metadataOptions = EnumSet.copyOf(metadataOptionList);
        }
        metadataOptions.addAll(defaultMetadataOptions); // default metadata options
        getFeaturesRequest.setMetadataOptions(metadataOptions);
      }
      return getFeaturesRequest;
    }

    private void validateRequestParameters() {
      if (this.joinKeyMap.isEmpty() && this.requestContextMap.isEmpty()) {
        throw new TectonClientException(TectonErrorMessage.EMPTY_REQUEST_MAPS);
      }
      if (!this.joinKeyMap.isEmpty()) {
        joinKeyMap.forEach(
            (key, value) -> {
              Validate.notEmpty(key, TectonErrorMessage.INVALID_KEY_VALUE);
              Validate.notEmpty(value, TectonErrorMessage.INVALID_KEY_VALUE);
            });
      }
      if (!this.requestContextMap.isEmpty()) {
        requestContextMap.forEach(
            (key, value) -> {
              Validate.notEmpty(key, TectonErrorMessage.INVALID_KEY_VALUE);
              Validate.notNull(value, TectonErrorMessage.INVALID_KEY_VALUE);
              if (value instanceof String) {
                Validate.notEmpty((String) value, TectonErrorMessage.INVALID_KEY_VALUE);
              }
            });
      }
    }
  }
}
