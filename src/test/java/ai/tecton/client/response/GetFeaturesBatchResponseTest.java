package ai.tecton.client.response;

import ai.tecton.client.model.SloInformation;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;
import java.util.stream.IntStream;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GetFeaturesBatchResponseTest {
  private String batchResponse1;
  private String batchResponse2;
  private String batchResponse3;
  GetFeaturesBatchResponse batchResponse;
  Map<String, Duration> responseToDurationMap;

  @Before
  public void setup() throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    String inputFile = classLoader.getResource("response/batch/batch1.json").getFile();
    batchResponse1 = new String(Files.readAllBytes(Paths.get(inputFile)));
    inputFile = classLoader.getResource("response/batch/batch2.json").getFile();
    batchResponse2 = new String(Files.readAllBytes(Paths.get(inputFile)));
    inputFile = classLoader.getResource("response/batch/batch3.json").getFile();
    batchResponse3 = new String(Files.readAllBytes(Paths.get(inputFile)));
    responseToDurationMap = new HashMap<>();
  }

  @Test
  public void testSingleMicroBatchResponseWithoutSloInfo() {
    responseToDurationMap.put(batchResponse1, Duration.ofMillis(10));
    batchResponse = new GetFeaturesBatchResponse(responseToDurationMap, true);
    List<GetFeaturesResponse> featureVectorList = batchResponse.getBatchResponseList();
    // Verify that there are 5 GetFeaturesResponse objects, each with a feature vector of 14
    // features
    Assert.assertEquals(7, featureVectorList.size());
    featureVectorList.forEach(
        getFeaturesResponse -> {
          Assert.assertEquals(14, getFeaturesResponse.getFeatureValues().size());
          Assert.assertFalse(getFeaturesResponse.getSloInformation().isPresent());
        });

    // Check that values are in order in the response list
    checkResultOrdering(featureVectorList);
    Assert.assertFalse(batchResponse.getBatchSloInformation().isPresent());
  }

  @Test
  public void testSingleMicroBatchResponseWithSloInfo() {
    responseToDurationMap.put(batchResponse2, Duration.ofMillis(10));
    batchResponse = new GetFeaturesBatchResponse(responseToDurationMap, true);
    List<GetFeaturesResponse> featureVectorList = batchResponse.getBatchResponseList();
    // Verify that there are 5 GetFeaturesResponse objects, each with a feature vector of 14
    // features
    Assert.assertEquals(5, featureVectorList.size());
    featureVectorList.forEach(
        getFeaturesResponse -> {
          Assert.assertEquals(14, getFeaturesResponse.getFeatureValues().size());
          Assert.assertTrue(getFeaturesResponse.getSloInformation().isPresent());
        });

    // Verify BatchSloInformation
    Assert.assertTrue(batchResponse.getBatchSloInformation().isPresent());
    SloInformation batchSloInfo = batchResponse.getBatchSloInformation().get();
    Assert.assertEquals(new Double(0.048292505), batchSloInfo.getSloServerTimeSeconds().get());
    Assert.assertTrue(batchSloInfo.isSloEligible().get());
    Assert.assertEquals(new Double(0.099455727), batchSloInfo.getServerTimeSeconds().get());
  }

  @Test
  public void testMultipleMicroBatchResponsesWithSlo() {
    responseToDurationMap.put(batchResponse2, Duration.ofMillis(10));
    responseToDurationMap.put(batchResponse3, Duration.ofMillis(8));

    batchResponse = new GetFeaturesBatchResponse(responseToDurationMap, true);
    List<GetFeaturesResponse> featureVectorList = batchResponse.getBatchResponseList();
    Assert.assertEquals(12, featureVectorList.size());
    checkBatchResultOrdering(featureVectorList);

    // Verify batch Slo info
    Assert.assertTrue(batchResponse.getBatchSloInformation().isPresent());
    SloInformation batchSloInfo = batchResponse.getBatchSloInformation().get();
    Assert.assertFalse(batchSloInfo.isSloEligible().get());
    Assert.assertEquals(1, batchSloInfo.getSloIneligibilityReasons().size());
    Assert.assertTrue(
        batchSloInfo
            .getSloIneligibilityReasons()
            .contains(SloInformation.SloIneligibilityReason.DYNAMODB_RESPONSE_SIZE_LIMIT_EXCEEDED));
    Assert.assertEquals(new Double(0.077513756), batchSloInfo.getSloServerTimeSeconds().get());
    Assert.assertEquals(new Double(0.099455727), batchSloInfo.getServerTimeSeconds().get());
  }

  @Test
  public void testMultipleSingleGetFeatures() throws Exception {
    Map<String, Duration> singGetFeaturesResponse = new HashMap<>();
    URL url = getClass().getClassLoader().getResource("response/single");
    Path path = Paths.get(url.toURI());
    Files.walk(path, 1)
        .forEach(
            file -> {
              try {
                singGetFeaturesResponse.put(
                    new String(Files.readAllBytes(file)), Duration.ofMillis(10));
              } catch (IOException ignored) {
              }
            });
    GetFeaturesBatchResponse batchResponse =
        new GetFeaturesBatchResponse(singGetFeaturesResponse, false);
    // Verify 4 GetFeaturesResponse in the list, each with a different feature vector size
    Assert.assertEquals(4, batchResponse.getBatchResponseList().size());
    List<Integer> featureVectorSizes = Arrays.asList(14, 5, 3, 5);
    List<GetFeaturesResponse> responseList = batchResponse.getBatchResponseList();
    IntStream.range(0, featureVectorSizes.size())
        .forEach(
            i -> {
              Assert.assertEquals(
                  featureVectorSizes.get(i).intValue(),
                  responseList.get(i).getFeatureValues().size());
            });

    // Assert batch slo info is empty
    Assert.assertFalse(batchResponse.getBatchSloInformation().isPresent());
  }

  private void checkResultOrdering(List<GetFeaturesResponse> batchResponse) {
    List<Double> valuesInOrder =
        Arrays.asList(
            0.0015965939329430547,
            0.01410105757931845,
            0.0,
            0.0030410542321338066,
            0.0,
            0.0,
            0.007851934941110488);
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

  private void checkBatchResultOrdering(List<GetFeaturesResponse> batchResponse) {
    List<Long> valuesInOrder =
        Arrays.asList(693L, 671L, 692L, 669L, null, 672L, 668L, 697L, 690L, 688L, 685L, 676L);
    String featureName =
        "user_distinct_merchant_transaction_count_30d.distinct_merchant_transaction_count_30d";
    IntStream.range(0, batchResponse.size())
        .parallel()
        .forEach(
            i -> {
              Assert.assertEquals(
                  valuesInOrder.get(i),
                  batchResponse.get(i).getFeatureValuesAsMap().get(featureName).int64value());
            });
  }
}
