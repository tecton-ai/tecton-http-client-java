package com.tecton.client.response;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.tecton.client.exceptions.TectonClientException;
import com.tecton.client.exceptions.TectonErrorMessage;
import com.tecton.client.model.FeatureServiceMetadata;
import com.tecton.client.model.NameAndType;
import com.tecton.client.model.ValueType;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class GetFeatureServiceMetadataResponse extends AbstractTectonResponse {

  private FeatureServiceMetadata featureServiceMetadata;
  private final JsonAdapter<GetFeatureServiceMetadataJson> jsonAdapter;

  public GetFeatureServiceMetadataResponse(String response, Duration requestLatency) {
    super(requestLatency);
    Moshi moshi = new Moshi.Builder().build();
    jsonAdapter = moshi.adapter(GetFeatureServiceMetadataJson.class);
    buildResponseFromJson(response);
  }

  public FeatureServiceMetadata getFeatureServiceMetadata() {
    return this.featureServiceMetadata;
  }

  @Override
  void buildResponseFromJson(String response) {
    GetFeatureServiceMetadataJson featureServiceMetadataJson;
    try {
      featureServiceMetadataJson = jsonAdapter.fromJson(response);
      this.featureServiceMetadata =
          new FeatureServiceMetadata(
              parseNameAndType(featureServiceMetadataJson.inputJoinKeys),
              parseNameAndType(featureServiceMetadataJson.inputRequestContextKeys),
              parseNameAndType(featureServiceMetadataJson.featureValues));

    } catch (IOException | NullPointerException e) {
      throw new TectonClientException(TectonErrorMessage.INVALID_RESPONSE_FORMAT);
    }
  }

  private void validateResponse(GetFeatureServiceMetadataJson featureServiceMetadataJson) {
    // TODO can all the maps in the response be empty?
  }

  static class GetFeatureServiceMetadataJson {
    private String featureServiceType;
    private List<NameAndTypeJson> inputJoinKeys;
    private List<NameAndTypeJson> inputRequestContextKeys;
    private List<NameAndTypeJson> outputJoinKeys;
    private List<NameAndTypeJson> featureValues;
    private String type;
  }

  static class NameAndTypeJson {
    String name;
    ResponseDataType dataType = new ResponseDataType();
  }

  private List<NameAndType> parseNameAndType(List<NameAndTypeJson> nameAndTypeJson) {
    List<NameAndType> nameAndTypeList = new ArrayList<>();
    if (nameAndTypeJson != null) {
      nameAndTypeJson.forEach(
          nt -> {
            ValueType dataValueType = nt.dataType.getDataType();
            if (dataValueType == ValueType.ARRAY) {
              ValueType elementValueType = nt.dataType.getListElementType().get();
              nameAndTypeList.add(new NameAndType(nt.name, dataValueType, elementValueType));
            } else {
              nameAndTypeList.add(new NameAndType(nt.name, dataValueType));
            }
          });
    }
    return nameAndTypeList;
  }
}
