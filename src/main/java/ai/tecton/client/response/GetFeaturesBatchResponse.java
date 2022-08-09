package ai.tecton.client.response;

import ai.tecton.client.exceptions.TectonClientException;
import ai.tecton.client.exceptions.TectonErrorMessage;
import ai.tecton.client.model.SloInformation;
import ai.tecton.client.response.GetFeaturesResponseUtils.FeatureMetadata;
import ai.tecton.client.response.GetFeaturesResponseUtils.FeatureVectorJson;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GetFeaturesBatchResponse extends AbstractTectonResponse {
  private List<GetFeaturesResponse> batchFeatureResponse;
  private SloInformation batchSloInformation;
  private final JsonAdapter<GetFeaturesBatchResponseJson> jsonAdapter;

  public GetFeaturesBatchResponse(String response, Duration requestLatency)
      throws TectonClientException {
    super(requestLatency);
    Moshi moshi = new Moshi.Builder().build();
    jsonAdapter = moshi.adapter(GetFeaturesBatchResponseJson.class);
    buildResponseFromJson(response);
  }

  public List<GetFeaturesResponse> getFeaturesResponseList() {
    return batchFeatureResponse;
  }

  public SloInformation getBatchSloInformation() {
    return this.batchSloInformation;
  }

  static class GetFeaturesBatchResponseJson {
    List<FeatureVectorJson> result;
    ResponseMetadataJson metadata;

    static class ResponseMetadataJson {
      List<FeatureMetadata> features;
      List<SloInformation> sloInfo;
      SloInformation batchSloInfo;
    }
  }

  @Override
  void buildResponseFromJson(String response) {
    GetFeaturesBatchResponseJson responseJson;
    try {
      responseJson = jsonAdapter.fromJson(response);
    } catch (IOException e) {
      throw new TectonClientException(TectonErrorMessage.INVALID_RESPONSE_FORMAT);
    }

    List<FeatureVectorJson> featureVectorJson = responseJson.result;
    List<FeatureMetadata> featureMetadata = responseJson.metadata.features;
    List<SloInformation> sloInformationList = responseJson.metadata.sloInfo;

    // Parallel Stream to map each feature vector and sloInfo in the response to a corresponding
    // GetFeaturesResponse object and collect to a List
    this.batchFeatureResponse =
        IntStream.range(0, responseJson.result.size())
            .parallel()
            .mapToObj(
                i ->
                    new GetFeaturesResponse(
                        GetFeaturesResponseUtils.constructFeatureVector(
                            featureVectorJson.get(i).features, featureMetadata),
                        sloInformationList.get(i),
                        this.getRequestLatency()))
            .collect(Collectors.toList());

    this.batchSloInformation = responseJson.metadata.batchSloInfo;
  }
}
