package ai.tecton.client.request;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import ai.tecton.client.exceptions.TectonClientException;
import ai.tecton.client.exceptions.TectonErrorMessage;
import ai.tecton.client.model.MetadataOption;
import ai.tecton.client.request.GetFeaturesBatchRequest.GetFeaturesMicroBatchRequest;
import ai.tecton.client.transport.TectonHttpClient;
import ai.tecton.client.utils.TestUtils;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GetFeaturesBatchRequestTest {

  private static final String TEST_WORKSPACENAME = "testWorkspaceName";
  private static final String TEST_FEATURESERVICE_NAME = "testFSName";
  private static final String BATCH_ENDPOINT = "/api/v1/feature-service/get-features-batch";
  private static final String ENDPOINT = "/api/v1/feature-service/get-features";

  GetFeaturesBatchRequest getFeaturesBatchRequest;
  List<GetFeaturesRequestData> defaultFeatureRequestDataList;

  @Before
  public void setup() {
    defaultFeatureRequestDataList = new ArrayList<>();
    GetFeaturesRequestData requestData = new GetFeaturesRequestData();
    requestData.addJoinKey("testKey", "testValue");
    defaultFeatureRequestDataList.add(requestData);
  }

  @Test
  public void testEmptyWorkspaceName_shouldThrowException() {
    try {
      getFeaturesBatchRequest =
          new GetFeaturesBatchRequest.Builder()
              .workspaceName("")
              .featureServiceName(TEST_FEATURESERVICE_NAME)
              .build();
      fail();
    } catch (TectonClientException e) {
      Assert.assertEquals(TectonErrorMessage.INVALID_WORKSPACENAME, e.getMessage());
    }
  }

  @Test
  public void testNullFeatureServiceName_shouldThrowException() {
    try {
      getFeaturesBatchRequest =
          new GetFeaturesBatchRequest.Builder()
              .workspaceName(TEST_WORKSPACENAME)
              .featureServiceName(null)
              .build();
      fail();
    } catch (TectonClientException e) {
      Assert.assertEquals(TectonErrorMessage.INVALID_FEATURESERVICENAME, e.getMessage());
    }
  }

  @Test
  public void testEmptyRequestDataList_shouldThrowException() {
    List<GetFeaturesRequestData> requestData = new ArrayList<>();
    try {
      getFeaturesBatchRequest =
          new GetFeaturesBatchRequest.Builder()
              .workspaceName(TEST_WORKSPACENAME)
              .featureServiceName(TEST_FEATURESERVICE_NAME)
              .requestDataList(requestData)
              .build();
      fail();
    } catch (TectonClientException e) {
      Assert.assertEquals(TectonErrorMessage.INVALID_REQUEST_DATA_LIST, e.getMessage());
    }
  }

  @Test
  public void testEmptyRequestData_shouldThrowException() {
    List<GetFeaturesRequestData> requestData = new ArrayList<>();
    requestData.add(new GetFeaturesRequestData());
    try {
      getFeaturesBatchRequest =
          new GetFeaturesBatchRequest.Builder()
              .workspaceName(TEST_WORKSPACENAME)
              .featureServiceName(TEST_FEATURESERVICE_NAME)
              .requestDataList(requestData)
              .build();
      fail();
    } catch (TectonClientException e) {
      Assert.assertEquals(TectonErrorMessage.EMPTY_REQUEST_MAPS, e.getMessage());
    }
  }

  @Test
  public void testInvalidJoinKey_shouldThrowException() {
    List<GetFeaturesRequestData> requestData = new ArrayList<>();
    try {
      requestData.add(new GetFeaturesRequestData().addJoinKey("", ""));
      getFeaturesBatchRequest =
          new GetFeaturesBatchRequest.Builder()
              .workspaceName(TEST_WORKSPACENAME)
              .featureServiceName(TEST_FEATURESERVICE_NAME)
              .requestDataList(requestData)
              .build();
      fail();
    } catch (IllegalArgumentException e) {
      Assert.assertEquals(TectonErrorMessage.INVALID_KEY_VALUE, e.getMessage());
    }
  }

  @Test
  public void testInvalidMicroBatchSize_shouldThrowException() {
    try {
      getFeaturesBatchRequest =
          new GetFeaturesBatchRequest.Builder()
              .workspaceName(TEST_WORKSPACENAME)
              .featureServiceName(TEST_FEATURESERVICE_NAME)
              .requestDataList(defaultFeatureRequestDataList)
              .microBatchSize(10)
              .build();
      fail();
    } catch (TectonClientException e) {
      Assert.assertEquals(
          String.format(TectonErrorMessage.INVALID_MICRO_BATCH_SIZE, 1, 5), e.getMessage());
    }
  }

  @Test
  public void testDefaultBatchRequest_shouldCallGetFeaturesEndpoint() {
    // GetFeaturesBatchRequest with 25 requestData should create 25 individual GetFeaturesRequest
    // objects
    List<GetFeaturesRequestData> requestDataList = TestUtils.generateRequestDataForSize(25);
    getFeaturesBatchRequest =
        new GetFeaturesBatchRequest(TEST_WORKSPACENAME, TEST_FEATURESERVICE_NAME, requestDataList);

    List<GetFeaturesRequest> getFeaturesRequests =
        (List<GetFeaturesRequest>) getFeaturesBatchRequest.getRequestList();
    Assert.assertEquals(25, getFeaturesRequests.size());

    // Check ordering of requests
    List<GetFeaturesRequestData> actualRequestDataList =
        getFeaturesRequests.stream()
            .map(GetFeaturesRequest::getFeaturesRequestData)
            .collect(Collectors.toList());
    Assert.assertEquals(requestDataList, actualRequestDataList);
  }

  @Test
  public void testBatchRequestWithSingleRequestData_shouldCallGetFeaturesEndpoint() {
    // GetFeaturesBatchRequest with one requestData in the list should create one GetFeaturesRequest
    // that calls
    // /get-features endpoint
    getFeaturesBatchRequest =
        new GetFeaturesBatchRequest(
            TEST_WORKSPACENAME,
            TEST_FEATURESERVICE_NAME,
            defaultFeatureRequestDataList,
            new HashSet<>(
                Arrays.asList(
                    MetadataOption.NAME, MetadataOption.DATA_TYPE, MetadataOption.SLO_INFO)),
            4);

    Assert.assertEquals(1, getFeaturesBatchRequest.getRequestList().size());
    Assert.assertEquals(RequestConstants.NONE_TIMEOUT, getFeaturesBatchRequest.getTimeout());

    GetFeaturesRequest getFeaturesRequest =
        (GetFeaturesRequest) getFeaturesBatchRequest.getRequestList().get(0);
    checkGetFeaturesCommonFields(
        getFeaturesRequest,
        ENDPOINT,
        EnumSet.of(MetadataOption.NAME, MetadataOption.DATA_TYPE, MetadataOption.SLO_INFO));
  }

  @Test
  public void testBatchRequestWithTwoRequestDataAndMicroBatchSize_shouldCallBatchEndpoint() {
    // GetFeaturesBatchRequest with two requestData and microBatchSize = 5 should create 1
    // GetFeaturesMicroBatchRequest
    // with a requestDatalist of size 2
    // ordering should be maintained
    GetFeaturesRequestData requestData = new GetFeaturesRequestData().addJoinKey("user", "123");
    getFeaturesBatchRequest =
        new GetFeaturesBatchRequest.Builder()
            .workspaceName(TEST_WORKSPACENAME)
            .featureServiceName(TEST_FEATURESERVICE_NAME)
            .requestDataList(defaultFeatureRequestDataList)
            .addRequestData(requestData)
            .metadataOptions(RequestConstants.DEFAULT_METADATA_OPTIONS)
            .microBatchSize(5)
            .build();

    Assert.assertEquals(1, getFeaturesBatchRequest.getRequestList().size());

    GetFeaturesMicroBatchRequest microBatchRequest =
        (GetFeaturesMicroBatchRequest) getFeaturesBatchRequest.getRequestList().get(0);
    checkGetFeaturesCommonFields(
        microBatchRequest,
        BATCH_ENDPOINT,
        EnumSet.of(MetadataOption.NAME, MetadataOption.DATA_TYPE));

    Assert.assertEquals(
        defaultFeatureRequestDataList.size(), microBatchRequest.getFeaturesRequestData().size());
    Assert.assertEquals(defaultFeatureRequestDataList, microBatchRequest.getFeaturesRequestData());
  }

  @Test
  public void testBatchRequestWithSixRequestData_shouldCallBatchEndpoint() {
    // GetFeaturesBatchRequest with 6 requestData and microBatchSize=5 should create 2
    // GetFeaturesMicroBatchRequests
    // with a requestDatalist of size 5 and 1 respectively
    // ordering should be maintained

    List<GetFeaturesRequestData> requestDataList = TestUtils.generateRequestDataForSize(6);
    getFeaturesBatchRequest =
        new GetFeaturesBatchRequest.Builder()
            .workspaceName(TEST_WORKSPACENAME)
            .featureServiceName(TEST_FEATURESERVICE_NAME)
            .requestDataList(requestDataList)
            .microBatchSize(5)
            .build();

    Assert.assertEquals(2, getFeaturesBatchRequest.getRequestList().size());

    List<GetFeaturesMicroBatchRequest> microBatchRequestList =
        (List<GetFeaturesMicroBatchRequest>) getFeaturesBatchRequest.getRequestList();
    List<GetFeaturesRequestData> firstList = microBatchRequestList.get(0).getFeaturesRequestData();
    List<GetFeaturesRequestData> secondList = microBatchRequestList.get(1).getFeaturesRequestData();

    // Verify requestData sizes in each microbatch
    Assert.assertEquals(5, firstList.size());
    Assert.assertEquals(1, secondList.size());

    // Verify request ordering
    Assert.assertEquals(requestDataList.subList(0, 5), firstList);
    Assert.assertEquals(requestDataList.subList(5, 6), secondList);
  }

  @Test
  public void testBatchRequestWithTwentyRequestData_shouldCallBatchEndpoint() {
    // GetFeaturesBatchRequest with 18 requests and microBatchSize of 5 should create 4
    // GetFeaturesMicroBatchRequests
    // with a requestDatalist of size of 5,5,5,3 respectively
    List<GetFeaturesRequestData> requestDataList = TestUtils.generateRequestDataForSize(18);

    getFeaturesBatchRequest =
        new GetFeaturesBatchRequest.Builder()
            .workspaceName(TEST_WORKSPACENAME)
            .featureServiceName(TEST_FEATURESERVICE_NAME)
            .requestDataList(requestDataList)
            .microBatchSize(5)
            .build();

    Assert.assertEquals(4, getFeaturesBatchRequest.getRequestList().size());

    List<GetFeaturesMicroBatchRequest> microBatchRequestList =
        (List<GetFeaturesMicroBatchRequest>) getFeaturesBatchRequest.getRequestList();
    List<GetFeaturesRequestData> first = microBatchRequestList.get(0).getFeaturesRequestData();
    List<GetFeaturesRequestData> second = microBatchRequestList.get(1).getFeaturesRequestData();
    List<GetFeaturesRequestData> third = microBatchRequestList.get(2).getFeaturesRequestData();
    List<GetFeaturesRequestData> fourth = microBatchRequestList.get(3).getFeaturesRequestData();

    // Verify sizes of each microbatch
    Assert.assertEquals(5, first.size());
    Assert.assertEquals(5, second.size());
    Assert.assertEquals(5, third.size());
    Assert.assertEquals(3, fourth.size());

    // Verify request ordering
    Assert.assertEquals(requestDataList.subList(0, 5), first);
    Assert.assertEquals(requestDataList.subList(5, 10), second);
    assertEquals(requestDataList.subList(10, 15), third);
  }

  @Test
  public void testBatchRequestWithMicroBatchSizeOne_shouldCallGetFeaturesEndpoint() {
    // GetFeaturesBatchRequest with 20 requests and microBatchSize of 1 should create 20 individual
    // GetFeaturesRequests
    getFeaturesBatchRequest =
        new GetFeaturesBatchRequest(
            TEST_WORKSPACENAME,
            TEST_FEATURESERVICE_NAME,
            TestUtils.generateRequestDataForSize(20),
            RequestConstants.DEFAULT_METADATA_OPTIONS,
            1);

    Assert.assertEquals(20, getFeaturesBatchRequest.getRequestList().size());
  }

  @Test
  public void testGivenBatchRequestObject_shouldSerializeToValidString() throws IOException {

    getFeaturesBatchRequest =
        new GetFeaturesBatchRequest.Builder()
            .requestDataList(TestUtils.generateFraudRequestDataFromFile("request/input.csv"))
            .workspaceName("prod")
            .featureServiceName("fraud_detection_feature_service")
            .metadataOptions(RequestConstants.ALL_METADATA_OPTIONS)
            .microBatchSize(5)
            .build();

    String expected_json =
        "{\"params\":{\"feature_service_name\":\"fraud_detection_feature_service\",\"metadata_options\":{\"include_slo_info\":true,\"include_effective_times\":true,\"include_names\":true,\"include_data_types\":true,\"include_serving_status\":true},\"request_data\":[{\"join_key_map\":{\"user_id\":\"user_656020174537\",\"merchant\":\"fraud_Cummerata-Jones\"},\"request_context_map\":{\"amt\":61.06}},{\"join_key_map\":{\"user_id\":\"user_394495759023\",\"merchant\":\"fraud_Marks Inc\"},\"request_context_map\":{\"amt\":106.43}},{\"join_key_map\":{\"user_id\":\"user_656020174537\",\"merchant\":\"fraud_Grimes LLC\"},\"request_context_map\":{\"amt\":24.95}},{\"join_key_map\":{\"user_id\":\"user_499975010057\",\"merchant\":\"fraud_Thiel Ltd\"},\"request_context_map\":{\"amt\":2.12}},{\"join_key_map\":{\"user_id\":\"user_656020174537\",\"merchant\":\"fraud_Bins-Rice\"},\"request_context_map\":{\"amt\":68.31}}],\"workspace_name\":\"prod\"}}";
    Assert.assertEquals(1, getFeaturesBatchRequest.getRequestList().size());

    GetFeaturesMicroBatchRequest microBatchRequest =
        (GetFeaturesMicroBatchRequest) getFeaturesBatchRequest.getRequestList().get(0);
    Assert.assertEquals(expected_json, microBatchRequest.requestToJson());
  }

  private void checkGetFeaturesCommonFields(
      AbstractGetFeaturesRequest getFeaturesRequest,
      String endpoint,
      Set<MetadataOption> metadataOptions) {
    Assert.assertEquals(endpoint, getFeaturesRequest.getEndpoint());
    Assert.assertEquals(TectonHttpClient.HttpMethod.POST, getFeaturesRequest.getMethod());
    Assert.assertEquals(TEST_WORKSPACENAME, getFeaturesRequest.getWorkspaceName());
    Assert.assertEquals(TEST_FEATURESERVICE_NAME, getFeaturesRequest.getFeatureServiceName());
    Assert.assertEquals(metadataOptions, getFeaturesRequest.getMetadataOptions());
  }
}
