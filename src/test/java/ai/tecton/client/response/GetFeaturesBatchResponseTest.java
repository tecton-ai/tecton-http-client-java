package ai.tecton.client.response;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GetFeaturesBatchResponseTest {
  private String sampleBatchResponse;
  GetFeaturesBatchResponse batchResponse;

  @Before
  public void setup() throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    String simpleInput = classLoader.getResource("response/sample_response_batch.json").getFile();
    sampleBatchResponse = new String(Files.readAllBytes(Paths.get(simpleInput)));
  }

  @Test
  public void testResponse() {
    batchResponse = new GetFeaturesBatchResponse(sampleBatchResponse, Duration.ofMillis(5));
    List<GetFeaturesResponse> featureVectorList = batchResponse.getFeaturesResponseList();
    // Verify that there are 5 GetFeaturesResponse objects, each with a feature vector of 14
    // features
    Assert.assertEquals(5, featureVectorList.size());
    featureVectorList.forEach(
        getFeaturesResponse -> {
          Assert.assertEquals(14, getFeaturesResponse.getFeatureValues().size());
          Assert.assertNotNull(getFeaturesResponse.getSloInformation());
        });

    // Check that values are in order in the response list
    checkResultOrdering(featureVectorList);
    Assert.assertNotNull(batchResponse.getBatchSloInformation());
  }

  private void checkResultOrdering(List<GetFeaturesResponse> batchResponse) {
    List<Double> valuesInOrder =
        Arrays.asList(0.0019035532994923859, 0.0005305039787798408, 0.0, 0.0, null);
    String featureName = "merchant_fraud_rate.is_fraud_mean_30d_1d";

    IntStream.range(0, batchResponse.size())
        .parallel()
        .forEach(
            i -> {
              Assert.assertEquals(
                  valuesInOrder.get(i),
                  batchResponse.get(i).getFeatureValuesAsMap().get(featureName).float64Value());
            });
  }
}
