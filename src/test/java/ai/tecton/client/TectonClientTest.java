package ai.tecton.client;

import static junit.framework.TestCase.fail;

import ai.tecton.client.exceptions.BadRequestException;
import ai.tecton.client.exceptions.ResourceExhaustedException;
import ai.tecton.client.exceptions.TectonClientException;
import ai.tecton.client.exceptions.TectonErrorMessage;
import ai.tecton.client.exceptions.TectonException;
import ai.tecton.client.exceptions.UnauthorizedException;
import ai.tecton.client.model.*;
import ai.tecton.client.request.*;
import ai.tecton.client.response.GetFeatureServiceMetadataResponse;
import ai.tecton.client.response.GetFeaturesBatchResponse;
import ai.tecton.client.response.GetFeaturesResponse;
import ai.tecton.client.utils.TestUtils;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.IntStream;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
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
  List<String> sampleResponses = new ArrayList<>();
  List<String> sampleBatchResponses = new ArrayList<>();

  @Before
  public void setup() throws IOException, URISyntaxException {
    this.apiKey = "12345";
    mockWebServer = new MockWebServer();
    mockWebServer.start();
    HttpUrl baseUrl = mockWebServer.url("");
    this.url = baseUrl.url().toString();
    classLoader = getClass().getClassLoader();
    tectonClient = new TectonClient(this.url, this.apiKey);
    sampleResponses = TestUtils.readAllFilesInDirectory("mocktest/getfeatures", "json");
    sampleBatchResponses = TestUtils.readAllFilesInDirectory("mocktest/getfeaturesbatch", "json");
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
    mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(sampleResponses.get(0)));

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
    Assert.assertEquals(FeatureStatus.PRESENT, sampleFeatureValue.getFeatureStatus().get());

    Assert.assertTrue(response.getSloInformation().isPresent());
    SloInformation sloInfo = response.getSloInformation().get();
    Assert.assertEquals(new Double(0.016342122), sloInfo.getServerTimeSeconds().get());
    Assert.assertEquals(new Double(0.014861452), sloInfo.getSloServerTimeSeconds().get());

    sampleFeatureValue = featureValueMap.get("user_transaction_amount_metrics.amt_sum_3d_10m");

    Assert.assertEquals(
        "user_transaction_amount_metrics", sampleFeatureValue.getFeatureNamespace());
    Assert.assertEquals("amt_sum_3d_10m", sampleFeatureValue.getFeatureName());
    Assert.assertEquals(ValueType.FLOAT64, sampleFeatureValue.getValueType());
    Assert.assertEquals(new Double(196112.57999999993), sampleFeatureValue.float64Value());
    Assert.assertEquals(FeatureStatus.CACHED, sampleFeatureValue.getFeatureStatus().get());

    Assert.assertTrue(response.getSloInformation().isPresent());
    sloInfo = response.getSloInformation().get();
    Assert.assertEquals(new Double(0.016342122), sloInfo.getServerTimeSeconds().get());
    Assert.assertEquals(new Double(0.014861452), sloInfo.getSloServerTimeSeconds().get());
  }

  @Test
  public void testTectonClientWithCustomOkHttpClient() throws IOException {
    // Test Setup
    mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(sampleResponses.get(0)));

    OkHttpClient httpClient = new OkHttpClient.Builder().build();
    TectonClient tectonClient = new TectonClient(this.url, this.apiKey, httpClient);

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
  }

  @Test
  public void testMetadataRequestAndResponse() throws IOException {
    // Test setup
    String responseFile =
        classLoader.getResource("mocktest/metadata/sampleMetadataResponse1.json").getFile();
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
    String expectedMessage = "Bad Request: Missing required join key: merchant";
    testErrorResponse(400, errorResponse, expectedMessage, BadRequestException.class);
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

    String expectedMessage =
        "Bad Request: Unable to query FeatureService `fraud_detection_feature` for workspace `prod`. Newly created feature services may take up to 60 seconds to query. Also, ensure that the workspace is a \"live\" workspace.";
    testErrorResponse(400, errorResponse, expectedMessage, BadRequestException.class);
  }

  @Test
  public void testInvalidApiKey() {
    String errorResponse =
        "{\"error\":\"invalid 'Tecton-key' authorization header\",\"code\":7,"
            + "\"message\":\"invalid 'Tecton-key' authorization header\"}";

    String expectedMessage = "Unauthorized: invalid 'Tecton-key' authorization header";

    testErrorResponse(401, errorResponse, expectedMessage, UnauthorizedException.class);
  }

  @Test
  public void testTooManyRequests() {
    String errorResponse =
        "{\"error\":\"FeatureService/GetFeatures exceeded the concurrent request limit, please retry later\",\"code\":8,"
            + "\"message\":\"FeatureService/GetFeatures exceeded the concurrent request limit, please retry later\"}";
    String expectedMessage =
        "FeatureService/GetFeatures exceeded the concurrent request limit, please retry later";

    testErrorResponse(429, errorResponse, expectedMessage, ResourceExhaustedException.class);
  }

  @Test
  public void testParallelGetFeaturesCall() {
    sampleResponses.forEach(
        sampleResponse ->
            mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(sampleResponse)));

    // Create request with 3 request data in the list
    GetFeaturesBatchRequest batchRequest =
        new GetFeaturesBatchRequest.Builder()
            .addRequestData(
                new GetFeaturesRequestData()
                    .addJoinKey("user_id", "111")
                    .addRequestContext("amount", 55.0))
            .addRequestData(
                new GetFeaturesRequestData()
                    .addJoinKey("user_id", "222")
                    .addRequestContext("amount", 155.0))
            .addRequestData(
                new GetFeaturesRequestData()
                    .addJoinKey("user_id", "333")
                    .addRequestContext("amount", 1000.0))
            .featureServiceName("fraud_detection_feature_service")
            .workspaceName("prod")
            .metadataOptions(RequestConstants.ALL_METADATA_OPTIONS)
            .build();

    Assert.assertEquals(1, batchRequest.getMicroBatchSize());
    GetFeaturesBatchResponse batchResponse = tectonClient.getFeaturesBatch(batchRequest);
    List<GetFeaturesResponse> responseList = batchResponse.getBatchResponseList();

    Assert.assertEquals(3, responseList.size());

    IntStream.range(0, sampleResponses.size())
        .forEach(
            i -> {
              GetFeaturesResponse getFeaturesResponse = responseList.get(i);
              Assert.assertEquals(14, getFeaturesResponse.getFeatureValues().size());
              for (FeatureValue value : getFeaturesResponse.getFeatureValues()) {
                Assert.assertTrue(value.getFeatureStatus().isPresent());
                Assert.assertTrue(
                    value.getFeatureStatus().get() == FeatureStatus.PRESENT
                        || value.getFeatureStatus().get() == FeatureStatus.CACHED);
              }
              Assert.assertTrue(getFeaturesResponse.getSloInformation().isPresent());
            });

    Assert.assertFalse(batchResponse.getBatchSloInformation().isPresent());
  }

  @Test
  public void testGetFeaturesBatchCall() throws IOException {
    sampleBatchResponses.forEach(
        sampleResponse ->
            mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(sampleResponse)));

    // Create request with 3 request data in the list
    List<GetFeaturesRequestData> requestDataList =
        TestUtils.generateFraudRequestDataFromFile("mocktest/getfeaturesbatch/input.csv");
    GetFeaturesBatchRequest batchRequest =
        new GetFeaturesBatchRequest(
            "prod",
            "fraud_detection_feature_service",
            requestDataList,
            RequestConstants.ALL_METADATA_OPTIONS,
            3);

    Assert.assertEquals(3, batchRequest.getMicroBatchSize());
    GetFeaturesBatchResponse batchResponse = tectonClient.getFeaturesBatch(batchRequest);
    List<GetFeaturesResponse> responseList = batchResponse.getBatchResponseList();

    Assert.assertEquals(7, responseList.size());

    IntStream.range(0, 7)
        .forEach(
            i -> {
              GetFeaturesResponse getFeaturesResponse = responseList.get(i);
              Assert.assertEquals(14, getFeaturesResponse.getFeatureValues().size());
              for (FeatureValue value : getFeaturesResponse.getFeatureValues()) {
                Assert.assertTrue(value.getFeatureStatus().isPresent());
                Assert.assertSame(value.getFeatureStatus().get(), FeatureStatus.PRESENT);
              }
              Assert.assertTrue(getFeaturesResponse.getSloInformation().isPresent());
            });

    Assert.assertTrue(batchResponse.getBatchSloInformation().isPresent());
  }

  private void testErrorResponse(
      int expectedStatusCode,
      String errorResponse,
      String expectedMessage,
      Class<? extends Exception> expectedException) {
    mockWebServer.enqueue(
        new MockResponse().setResponseCode(expectedStatusCode).setBody(errorResponse));

    GetFeatureServiceMetadataRequest getFeatureServiceMetadataRequest =
        new GetFeatureServiceMetadataRequest(FEATURE_SERVICE_NAME);

    try {
      GetFeatureServiceMetadataResponse response =
          tectonClient.getFeatureServiceMetadata(getFeatureServiceMetadataRequest);
      fail();
    } catch (Exception e) {
      Assert.assertTrue(expectedException.isInstance(e));
      Assert.assertEquals(
          Integer.valueOf(expectedStatusCode), ((TectonException) e).getStatusCode().get());
      Assert.assertEquals(expectedMessage, e.getMessage());
    }
  }
}
