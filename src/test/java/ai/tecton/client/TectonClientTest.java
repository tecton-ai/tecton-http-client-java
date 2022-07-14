package ai.tecton.client;

import static junit.framework.TestCase.fail;

import ai.tecton.client.exceptions.TectonClientException;
import ai.tecton.client.exceptions.TectonErrorMessage;
import ai.tecton.client.exceptions.TectonServiceException;
import ai.tecton.client.model.*;
import ai.tecton.client.request.GetFeatureServiceMetadataRequest;
import ai.tecton.client.request.GetFeaturesRequest;
import ai.tecton.client.request.GetFeaturesRequestData;
import ai.tecton.client.response.GetFeatureServiceMetadataResponse;
import ai.tecton.client.response.GetFeaturesResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TectonClientTest {
  private String url;
  private String apiKey;
  private MockWebServer mockWebServer;
  private ClassLoader classLoader;
  private static final String FEATURE_SERVICE_NAME = "fraud_detection_feature_service";
  private static final String WORKSPACE_NAME = "prod";
  TectonClient tectonClient;

  @Before
  public void setup() throws IOException {
    this.url = "https://test-url.com";
    this.apiKey = "12345";
    mockWebServer = new MockWebServer();
    mockWebServer.start();
    HttpUrl baseUrl = mockWebServer.url("");
    classLoader = getClass().getClassLoader();
    tectonClient = new TectonClient(baseUrl.url().toString(), "12345");
  }

  @Test
  public void testEmptyUrl() {
    try {
      TectonClient tectonClient = new TectonClient("", apiKey);
      fail();
    } catch (TectonClientException e) {
      Assert.assertEquals(TectonErrorMessage.INVALID_URL, e.getMessage());
    }
  }

  @Test
  public void testEmptyKey() {
    try {
      TectonClient tectonClient = new TectonClient(url, "");
      fail();
    } catch (TectonClientException e) {
      Assert.assertEquals(TectonErrorMessage.INVALID_KEY, e.getMessage());
    }
  }

  @Test
  public void testNullKey() {
    try {
      TectonClient tectonClient = new TectonClient(url, null);
      fail();
    } catch (TectonClientException e) {
      Assert.assertEquals(TectonErrorMessage.INVALID_KEY, e.getMessage());
    }
  }

  @Test
  public void testGetFeatureRequestAndResponse() throws IOException {
    // Test Setup
    String responseFile = classLoader.getResource("mocktest/sampleFeatureResponse1.json").getFile();
    String responseBody = new String(Files.readAllBytes(Paths.get(responseFile)));
    mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(responseBody));

    // Create Request Data
    GetFeaturesRequestData requestData =
        new GetFeaturesRequestData()
            .addRequestContext("amt", 100.55)
            .addJoinKey("user_id", "xyz")
            .addJoinKey("merchant", "abc");

    // Create GetFeaturesRequest
    GetFeaturesRequest request =
        new GetFeaturesRequest(WORKSPACE_NAME, FEATURE_SERVICE_NAME, requestData);

    // Send request and receive response
    GetFeaturesResponse response = tectonClient.getFeatures(request);

    // Feature Vector as List
    List<FeatureValue> featureValues = response.getFeatureValues();
    Assert.assertEquals(14, featureValues.size());

    // Feature Vector as Map
    Map<String, FeatureValue> featureValueMap = response.getFeatureValuesAsMap();

    // Sample Feature Value
    FeatureValue sampleFeatureValue =
        featureValueMap.get("user_transaction_amount_metrics.amt_sum_1h_10m");
    Assert.assertEquals(
        "user_transaction_amount_metrics", sampleFeatureValue.getFeatureNamespace());
    Assert.assertEquals("amt_sum_1h_10m", sampleFeatureValue.getFeatureName());
    Assert.assertEquals(ValueType.FLOAT64, sampleFeatureValue.getValueType());
    Assert.assertEquals(new Double(5817.029999999999), sampleFeatureValue.float64Value());

    Assert.assertTrue(response.getSloInformation().isPresent());
    SloInformation sloInfo = response.getSloInformation().get();
    Assert.assertEquals(new Double(0.016342122), sloInfo.getServerTimeSeconds().get());
    Assert.assertEquals(new Double(0.014861452), sloInfo.getSloServerTimeSeconds().get());
  }

  @Test
  public void testMetadataRequestAndResponse() throws IOException {
    // Test setup
    String responseFile =
        classLoader.getResource("mocktest/sampleMetadataResponse1.json").getFile();
    String responseBody = new String(Files.readAllBytes(Paths.get(responseFile)));
    mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(responseBody));

    // Create request
    GetFeatureServiceMetadataRequest getFeatureServiceMetadataRequest =
        new GetFeatureServiceMetadataRequest(FEATURE_SERVICE_NAME);

    // Send request and receive response
    GetFeatureServiceMetadataResponse response =
        tectonClient.getFeatureServiceMetadata(getFeatureServiceMetadataRequest);
    FeatureServiceMetadata featureServiceMetadata = response.getFeatureServiceMetadata();

    // Get input_join_keys
    Map<String, NameAndType> inputJoinKeys = featureServiceMetadata.getInputJoinKeysAsMap();
    Assert.assertEquals(2, inputJoinKeys.size());
    Assert.assertTrue(inputJoinKeys.containsKey("user_id"));
    Assert.assertTrue(inputJoinKeys.containsKey("merchant"));

    // Get input_request_context_keys
    Map<String, NameAndType> inputRequestContextKeys =
        featureServiceMetadata.getInputRequestContextKeysAsMap();
    Assert.assertEquals(1, inputRequestContextKeys.size());
    Assert.assertTrue(inputRequestContextKeys.containsKey("amt"));
    Assert.assertEquals(ValueType.FLOAT64, inputRequestContextKeys.get("amt").getDataType());
    Assert.assertEquals(Optional.empty(), inputRequestContextKeys.get("amt").getListElementType());

    // Get feature metadata as map
    Map<String, NameAndType> features = featureServiceMetadata.getFeatureValuesAsMap();
    Assert.assertEquals(14, features.size());

    List<NameAndType> expectedFeatures = new ArrayList<>();
    expectedFeatures.add(
        new NameAndType("user_transaction_counts.transaction_count_90d_1d", ValueType.INT64));
    expectedFeatures.add(
        new NameAndType("merchant_fraud_rate.is_fraud_mean_1d_1d", ValueType.FLOAT64));
    expectedFeatures.add(
        new NameAndType(
            "transaction_amount_is_higher_than_average.transaction_amount_is_higher_than_average",
            ValueType.BOOLEAN));

    // Get feature metadata as list and iterate through it
    expectedFeatures.forEach(
        feature ->
            Assert.assertTrue(
                featureServiceMetadata.getFeatureValues().stream()
                    .anyMatch(
                        featureVal ->
                            (featureVal.getName().equals(feature.getName())
                                && featureVal.getDataType() == feature.getDataType()))));
  }

  @Test
  public void testErrorResponseWhenJoinKeyIsMissing() {
    String errorResponse =
        "{\"error\":\"Missing required join key: merchant\",\"code\":3,\"message\":\"Missing required join key: merchant\"}";

    mockWebServer.enqueue(
        new MockResponse()
            .setResponseCode(400)
            .setHeader("Content-Type", "application/json")
            .setBody(errorResponse));

    GetFeaturesRequestData requestData =
        new GetFeaturesRequestData().addJoinKey("user_id", "12345").addRequestContext("amt", 555.5);
    GetFeaturesRequest request =
        new GetFeaturesRequest(WORKSPACE_NAME, FEATURE_SERVICE_NAME, requestData);
    String expectedMessage =
        "Received Error Response from Tecton wih code 400 and error message: Missing required join key: merchant";
    try {
      GetFeaturesResponse response = tectonClient.getFeatures(request);
      fail();
    } catch (TectonServiceException e) {
      Assert.assertEquals(expectedMessage, e.getMessage());
    }
  }

  @Test
  public void testErrorResponseWhenFeatureServiceDoesNotExist() {
    String errorResponse =
        "{\"error\":\"Unable to query FeatureService `fraud_detection_feature` for workspace `prod`. "
            + "Newly created feature services may take up to 60 seconds to query. "
            + "Also, ensure that the workspace is a \\\"live\\\" workspace.\",\"code\":5,"
            + "\"message\":\"Unable to query FeatureService `fraud_detection_feature` for workspace `prod`. "
            + "Newly created feature services may take up to 60 seconds to query."
            + " Also, ensure that the workspace is a \\\"live\\\" workspace.\"}";

    mockWebServer.enqueue(new MockResponse().setResponseCode(400).setBody(errorResponse));

    GetFeatureServiceMetadataRequest getFeatureServiceMetadataRequest =
        new GetFeatureServiceMetadataRequest(FEATURE_SERVICE_NAME);
    String expectedMessage =
        "Received Error Response from Tecton wih code 400 and error message: "
            + "Unable to query FeatureService `fraud_detection_feature` for workspace `prod`."
            + " Newly created feature services may take up to 60 seconds to query. "
            + "Also, ensure that the workspace is a \"live\" workspace.";
    try {
      GetFeatureServiceMetadataResponse response =
          tectonClient.getFeatureServiceMetadata(getFeatureServiceMetadataRequest);
      fail();
    } catch (TectonServiceException e) {
      Assert.assertEquals(expectedMessage, e.getMessage());
    }
  }

  @Test
  public void testInvalidApiKey() {
    String errorResponse =
        "{\"error\":\"invalid 'Tecton-key' authorization header\",\"code\":7,"
            + "\"message\":\"invalid 'Tecton-key' authorization header\"}";
    mockWebServer.enqueue(new MockResponse().setResponseCode(400).setBody(errorResponse));

    GetFeatureServiceMetadataRequest getFeatureServiceMetadataRequest =
        new GetFeatureServiceMetadataRequest(FEATURE_SERVICE_NAME);
    String expectedMessage =
        "Received Error Response from Tecton wih code 400 and error message: invalid 'Tecton-key' authorization header";

    try {
      GetFeatureServiceMetadataResponse response =
          tectonClient.getFeatureServiceMetadata(getFeatureServiceMetadataRequest);
      fail();
    } catch (TectonServiceException e) {
      Assert.assertEquals(expectedMessage, e.getMessage());
    }
  }
}
