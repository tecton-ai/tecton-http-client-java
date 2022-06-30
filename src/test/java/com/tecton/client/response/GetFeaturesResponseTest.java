package com.tecton.client.response;

import com.tecton.client.exceptions.TectonClientException;
import com.tecton.client.exceptions.TectonErrorMessage;
import com.tecton.client.model.FeatureValue;
import com.tecton.client.model.SloInformation;
import com.tecton.client.model.SloInformation.SloIneligibilityReason;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GetFeaturesResponseTest {

  GetFeaturesResponse getFeaturesResponse;
  String simpleResponse;
  String responseWithSlo;
  String responseWithArray;

  @Before
  public void setup() throws Exception {
    ClassLoader classLoader = getClass().getClassLoader();
    String simpleInput = classLoader.getResource("sample_response.json").getFile();
    simpleResponse = new String(Files.readAllBytes(Paths.get(simpleInput)));
    String sloInput = classLoader.getResource("sample_response_slo.json").getFile();
    responseWithSlo = new String(Files.readAllBytes(Paths.get(sloInput)));
    String arrayInput = classLoader.getResource("sample_response_list.json").getFile();
    responseWithArray = new String(Files.readAllBytes(Paths.get(arrayInput)));
  }

  @Test
  public void testSimpleResponse() {
    Duration duration = Duration.ofMillis(10);
    getFeaturesResponse = new GetFeaturesResponse(simpleResponse, duration);

    Assert.assertEquals(duration, getFeaturesResponse.getRequestLatency());
    Assert.assertFalse(getFeaturesResponse.getSloInformation().isPresent());
    checkFeatureValues(getFeaturesResponse.getFeatureValuesAsMap());
  }

  @Test
  public void testSloresponse() {
    Duration duration = Duration.ofMillis(10);
    getFeaturesResponse = new GetFeaturesResponse(responseWithSlo, duration);
    checkFeatureValues(getFeaturesResponse.getFeatureValuesAsMap());
    SloInformation sloInfo = getFeaturesResponse.getSloInformation().get();

    Assert.assertFalse(sloInfo.isSloEligible().get());
    Assert.assertEquals(new Double(0.034437937), sloInfo.getServerTimeSeconds().get());
    Assert.assertEquals(new Integer(13100000), sloInfo.getStoreResponseSizeBytes().get());
    Assert.assertEquals(1, sloInfo.getSloIneligibilityReasons().size());
    Assert.assertTrue(
        sloInfo
            .getSloIneligibilityReasons()
            .contains(SloIneligibilityReason.DYNAMODB_RESPONSE_SIZE_LIMIT_EXCEEDED));
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

    getFeaturesResponse = new GetFeaturesResponse(responseWithArray, duration);
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
}
