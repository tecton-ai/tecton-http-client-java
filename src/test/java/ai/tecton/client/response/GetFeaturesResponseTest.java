package ai.tecton.client.response;

import ai.tecton.client.exceptions.TectonClientException;
import ai.tecton.client.exceptions.TectonErrorMessage;
import ai.tecton.client.model.FeatureValue;
import ai.tecton.client.model.SloInformation;
import ai.tecton.client.utils.TestUtils;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GetFeaturesResponseTest {

  GetFeaturesResponse getFeaturesResponse;
  List<String> sampleResponses;

  @Before
  public void setup() throws Exception {
    sampleResponses = TestUtils.readAllFilesInDirectory("response/single", "json");
  }

  @Test
  public void testSimpleResponse() {
    Duration duration = Duration.ofMillis(10);
    getFeaturesResponse = new GetFeaturesResponse(sampleResponses.get(1), duration);

    Assert.assertEquals(duration, getFeaturesResponse.getRequestLatency());
    Assert.assertFalse(getFeaturesResponse.getSloInformation().isPresent());
    checkFeatureValues(getFeaturesResponse.getFeatureValuesAsMap());
  }

  @Test
  public void testSimpleResponseWithNulls() {
    Duration duration = Duration.ofMillis(10);
    getFeaturesResponse = new GetFeaturesResponse(sampleResponses.get(0), duration);
    Assert.assertEquals(14, getFeaturesResponse.getFeatureValues().size());
    Map<String, FeatureValue> featureValueMap = getFeaturesResponse.getFeatureValuesAsMap();

    // Check float64 null value
    Double doubleValue =
        featureValueMap.get("merchant_fraud_rate.is_fraud_mean_30d_1d").float64Value();
    Assert.assertNull(doubleValue);

    // Check int64 null value
    Long longValue =
        featureValueMap.get("user_transaction_counts.transaction_count_1d_1d").int64value();
    Assert.assertNull(longValue);
  }

  @Test
  public void testSloresponse() {
    Duration duration = Duration.ofMillis(10);
    getFeaturesResponse = new GetFeaturesResponse(sampleResponses.get(4), duration);
    checkFeatureValues(getFeaturesResponse.getFeatureValuesAsMap());
    SloInformation sloInfo = getFeaturesResponse.getSloInformation().get();

    Assert.assertFalse(sloInfo.isSloEligible().get());
    Assert.assertEquals(new Double(0.034437937), sloInfo.getServerTimeSeconds().get());
    Assert.assertEquals(new Integer(13100000), sloInfo.getStoreResponseSizeBytes().get());
    Assert.assertEquals(1, sloInfo.getSloIneligibilityReasons().size());
    Assert.assertTrue(
        sloInfo
            .getSloIneligibilityReasons()
            .contains(SloInformation.SloIneligibilityReason.DYNAMODB_RESPONSE_SIZE_LIMIT_EXCEEDED));
  }

  @Test
  public void testArrayValueResponse() {
    Duration duration = Duration.ofMillis(5);
    List<Double> expectedDoubleArray =
        new ArrayList<Double>() {
          {
            add(55.5);
            add(57.88);
            add(58.96);
            add(57.66);
            add(null);
            add(55.98);
          }
        };
    List<String> expectedStringArray = new ArrayList<>();
    expectedStringArray.add(null);
    List<Long> expectedLongArray = Collections.singletonList(0L);

    getFeaturesResponse = new GetFeaturesResponse(sampleResponses.get(2), duration);
    Assert.assertNotNull(getFeaturesResponse);
    Assert.assertEquals(3, getFeaturesResponse.getFeatureValues().size());

    List<Double> actualDoubleArray =
        getFeaturesResponse
            .getFeatureValuesAsMap()
            .get("average_rain.average_temperate_6hrs")
            .float64ArrayValue();
    Assert.assertEquals(expectedDoubleArray, actualDoubleArray);

    List<String> actualStringArray =
        getFeaturesResponse
            .getFeatureValuesAsMap()
            .get("average_rain.cloud_type")
            .stringArrayValue();
    Assert.assertEquals(expectedStringArray, actualStringArray);

    List<Long> actualLongArray =
        getFeaturesResponse
            .getFeatureValuesAsMap()
            .get("average_rain.rain_in_last_24_hrs")
            .int64ArrayValue();
    Assert.assertEquals(expectedLongArray, actualLongArray);
  }

  @Test
  public void testEqualsAndHashCode() {
    GetFeaturesResponse getFeaturesResponse =
        new GetFeaturesResponse(sampleResponses.get(1), Duration.ofMillis(10));
    GetFeaturesResponse getFeaturesResponseEquals =
        new GetFeaturesResponse(sampleResponses.get(1), Duration.ofMillis(5));
    GetFeaturesResponse getFeaturesResponseNotEquals =
        new GetFeaturesResponse(sampleResponses.get(2), Duration.ofMillis(10));

    Assert.assertEquals(getFeaturesResponse, getFeaturesResponseEquals);
    Assert.assertEquals(getFeaturesResponse.hashCode(), getFeaturesResponseEquals.hashCode());
    Assert.assertNotEquals(getFeaturesResponse, getFeaturesResponseNotEquals);
    Assert.assertNotEquals(getFeaturesResponse.hashCode(), getFeaturesResponseNotEquals.hashCode());
  }

  private void checkFeatureValues(Map<String, FeatureValue> featureValues) {
    Assert.assertEquals(5, getFeaturesResponse.getFeatureValues().size());
    Assert.assertEquals(
        new Long(0), featureValues.get("average_rain.rain_in_last_24_hrs").int64value());
    Assert.assertEquals(
        Boolean.FALSE,
        featureValues.get("average_rain.precipitation_higher_than_average").booleanValue());
    Assert.assertNull(featureValues.get("average_rain.atmospheric_pressure").float64Value());
    Assert.assertEquals(featureValues.get("average_rain.cloud_type").stringValue(), "nimbostratus");
    Assert.assertEquals(
        featureValues.get("average_rain.average_temperate_24hrs").float64Value(), new Double(55.5));
    try {
      featureValues.get("average_rain.average_temperate_24hrs").booleanValue();
    } catch (TectonClientException e) {
      Assert.assertEquals(
          String.format(TectonErrorMessage.MISMATCHED_TYPE, "float64"), e.getMessage());
    }
  }

  @Test
  public void testResponseWithNullArray() {
    Duration duration = Duration.ofMillis(10);
    getFeaturesResponse = new GetFeaturesResponse(sampleResponses.get(3), duration);
    Assert.assertEquals(4, getFeaturesResponse.getFeatureValues().size());
    Map<String, FeatureValue> featureValueMap = getFeaturesResponse.getFeatureValuesAsMap();
    Assert.assertNull(featureValueMap.get("average_rain.cloud_type").stringArrayValue());
    Assert.assertNull(featureValueMap.get("average_rain.cloud_number").int64ArrayValue());
    Assert.assertNull(featureValueMap.get("average_rain.precipitation").float32ArrayValue());
    Assert.assertNull(featureValueMap.get("average_rain.rainfall").float64ArrayValue());
  }
}
