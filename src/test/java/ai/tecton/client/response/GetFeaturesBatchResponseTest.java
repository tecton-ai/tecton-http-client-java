package ai.tecton.client.response;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.tecton.client.model.SloInformation;
import ai.tecton.client.transport.HttpResponse;
import ai.tecton.client.utils.TestUtils;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.*;
import java.util.stream.IntStream;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GetFeaturesBatchResponseTest {
  GetFeaturesBatchResponse batchResponse;
  List<String> batchResponses;

  @Before
  public void setup() throws IOException, URISyntaxException {
    batchResponses = TestUtils.readAllFilesInDirectory("response/batch", "json");
  }

  @Test
  public void testSingleMicroBatchResponseWithoutSloInfo() {
    List<HttpResponse> httpResponseList =
        generateHttpResponseList(Collections.singletonList(batchResponses.get(0)));
    batchResponse = new GetFeaturesBatchResponse(httpResponseList, Duration.ofMillis(25), 10);
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

    // Check batchSloInfo is Optional.empty()
    Assert.assertFalse(batchResponse.getBatchSloInformation().isPresent());
  }

  @Test
  public void testSingleMicroBatchResponseWithSloInfo() {
    List<HttpResponse> httpResponseList =
        generateHttpResponseList(Collections.singletonList(batchResponses.get(1)));
    batchResponse = new GetFeaturesBatchResponse(httpResponseList, Duration.ofMillis(25), 8);
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
    List<HttpResponse> httpResponseList =
        generateHttpResponseList(Arrays.asList(batchResponses.get(1), batchResponses.get(2)));
    batchResponse = new GetFeaturesBatchResponse(httpResponseList, Duration.ofMillis(25), 10);

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
    List<String> singleVectorResponseList =
        TestUtils.readAllFilesInDirectory("response/single", "json");
    List<HttpResponse> httpResponseList = generateHttpResponseList(singleVectorResponseList);
    GetFeaturesBatchResponse batchResponse =
        new GetFeaturesBatchResponse(httpResponseList, Duration.ofMillis(10), 1);
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
  public void testSingleGetFeaturesWithNulls() throws Exception {

    List<String> singleVectorResponseList =
        TestUtils.readAllFilesInDirectory("response/single", "json");
    List<HttpResponse> httpResponseList = generateHttpResponseList(singleVectorResponseList);

    // Add two null httpResponse to the list, corresponding to timeouts
    httpResponseList.add(null);
    httpResponseList.add(null);

    GetFeaturesBatchResponse batchResponse =
        new GetFeaturesBatchResponse(httpResponseList, Duration.ofMillis(100), 1);
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

  private List<HttpResponse> generateHttpResponseList(List<String> responseJsonList) {
    List<HttpResponse> httpResponseList = new ArrayList<>(responseJsonList.size());
    responseJsonList.forEach(
        responseJson -> {
          HttpResponse httpResponse = mock(HttpResponse.class);
          when(httpResponse.getResponseBody()).thenReturn(Optional.of(responseJson));
          when(httpResponse.isSuccessful()).thenReturn(true);
          when(httpResponse.getRequestDuration()).thenReturn(Duration.ofMillis(10));
          httpResponseList.add(httpResponse);
        });
    return httpResponseList;
  }
}
