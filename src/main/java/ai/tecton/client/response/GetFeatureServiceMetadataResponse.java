package ai.tecton.client.response;

import ai.tecton.client.exceptions.TectonClientException;
import ai.tecton.client.exceptions.TectonErrorMessage;
import ai.tecton.client.model.FeatureServiceMetadata;
import ai.tecton.client.model.NameAndType;
import ai.tecton.client.model.ValueType;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * A class that represents the response from the HTTP API for a call to the <i>/metadata</i>
 * endpoint.
 */
public class GetFeatureServiceMetadataResponse extends AbstractTectonResponse {

  private FeatureServiceMetadata featureServiceMetadata;
  private final JsonAdapter<GetFeatureServiceMetadataJson> jsonAdapter;

  public GetFeatureServiceMetadataResponse(String response, Duration requestLatency) {
    super(requestLatency);
    Moshi moshi = new Moshi.Builder().build();
    jsonAdapter = moshi.adapter(GetFeatureServiceMetadataJson.class);
    buildResponseFromJson(response);
  }

  /** Returns a {@link FeatureServiceMetadata} object that represents the metadata returned */
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
