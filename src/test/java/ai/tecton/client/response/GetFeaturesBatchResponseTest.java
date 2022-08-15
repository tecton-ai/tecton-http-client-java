package ai.tecton.client.response;

import ai.tecton.client.model.SloInformation;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;
import java.util.stream.IntStream;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GetFeaturesBatchResponseTest {
  private String batchResponse1;
  private String batchResponse2;
  private String batchResponse3;
  GetFeaturesBatchResponse batchResponse;
  List<Pair<String, Duration>> responseDurationPair;

  @Before
  public void setup() throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    String inputFile = classLoader.getResource("response/batch/batch1.json").getFile();
    batchResponse1 = new String(Files.readAllBytes(Paths.get(inputFile)));
    inputFile = classLoader.getResource("response/batch/batch2.json").getFile();
    batchResponse2 = new String(Files.readAllBytes(Paths.get(inputFile)));
    inputFile = classLoader.getResource("response/batch/batch3.json").getFile();
    batchResponse3 = new String(Files.readAllBytes(Paths.get(inputFile)));
    responseDurationPair = new ArrayList<>();
  }

  @Test
  public void testSingleMicroBatchResponseWithoutSloInfo() {
    responseDurationPair.add(Pair.of(batchResponse1, Duration.ofMillis(10)));
    batchResponse = new GetFeaturesBatchResponse(responseDurationPair, true);
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
    responseDurationPair.add(Pair.of(batchResponse2, Duration.ofMillis(10)));
    batchResponse = new GetFeaturesBatchResponse(responseDurationPair, true);
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
  public void testMultipleMicroBatchResponsesWithSloWithError() {
    responseDurationPair.add(Pair.of(batchResponse2, Duration.ofMillis(10)));
    responseDurationPair.add(Pair.of(batchResponse3, Duration.ofMillis(8)));

    batchResponse = new GetFeaturesBatchResponse(responseDurationPair, true);
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
    responseDurationPair = readAllResponsesFromDirectory("response/single");
    GetFeaturesBatchResponse batchResponse =
        new GetFeaturesBatchResponse(responseDurationPair, false);
    // Verify 4 GetFeaturesResponse in the list, each with a different feature vector size
    Assert.assertEquals(4, batchResponse.getBatchResponseList().size());
    List<Integer> featureVectorSizes = Arrays.asList(14, 5, 3, 5);
    List<GetFeaturesResponse> responseList = batchResponse.getBatchResponseList();
    IntStream.range(0, featureVectorSizes.size())
        .forEach(
            i ->
                Assert.assertEquals(
                    featureVectorSizes.get(i).intValue(),
                    responseList.get(i).getFeatureValues().size()));

    // Assert batch slo info is empty
    Assert.assertFalse(batchResponse.getBatchSloInformation().isPresent());
  }

  @Test
  public void testSingleGetFeaturesWithError() throws Exception {

    responseDurationPair = readAllResponsesFromDirectory("response/single");

    // Add two error responses to the map
    responseDurationPair.add(Pair.of(null, Duration.ofMillis(10)));
    responseDurationPair.add(Pair.of(null, Duration.ofMillis(15)));

    GetFeaturesBatchResponse batchResponse =
        new GetFeaturesBatchResponse(responseDurationPair, false);
    // Verify 6 GetFeaturesResponse in the list, 4 non-null and last two indices with null responses
    List<GetFeaturesResponse> responseList = batchResponse.getBatchResponseList();
    Assert.assertEquals(6, responseList.size());
    responseList.subList(0, 4).forEach(Assert::assertNotNull);
    responseList.subList(4, 6).forEach(Assert::assertNull);
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
              Long actualValue =
                  batchResponse.get(i).getFeatureValuesAsMap().get(featureName).int64value();
              Assert.assertEquals(valuesInOrder.get(i), actualValue);
            });
  }

  private List<Pair<String, Duration>> readAllResponsesFromDirectory(String directoryPath)
      throws IOException, URISyntaxException {
    List<Pair<String, Duration>> singGetFeaturesResponse = new ArrayList<>();
    URL url = getClass().getClassLoader().getResource(directoryPath);
    Path path = Paths.get(url.toURI());
    Files.walk(path, 1)
        .sorted()
        .forEach(
            file -> {
              try {
                singGetFeaturesResponse.add(
                    Pair.of(new String(Files.readAllBytes(file)), Duration.ofMillis(25)));
              } catch (IOException ignored) {
              }
            });
    return singGetFeaturesResponse;
  }
}
