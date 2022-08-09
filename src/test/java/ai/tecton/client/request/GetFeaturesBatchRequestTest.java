package ai.tecton.client.request;

import static org.junit.Assert.fail;

import ai.tecton.client.exceptions.TectonClientException;
import ai.tecton.client.exceptions.TectonErrorMessage;
import ai.tecton.client.model.MetadataOption;
import ai.tecton.client.transport.TectonHttpClient;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
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
  public void setup() throws IOException {
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
    } catch (IllegalArgumentException e) {
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
    } catch (NullPointerException e) {
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
              .microBatchSize(15)
              .build();
      fail();
    } catch (TectonClientException e) {
      Assert.assertEquals(
          String.format(TectonErrorMessage.INVALID_MICRO_BATCH_SIZE, 1, 10), e.getMessage());
    }
  }

  @Test
  public void testBatchRequestWithSingleRequestData_shouldCallGetFeaturesEndpoint() {
    // GetFeaturesBatchRequest with one requestData in the list should create one GetFeaturesRequest
    // that calls
    // /get-features endpoint
    getFeaturesBatchRequest =
        new GetFeaturesBatchRequest.Builder()
            .workspaceName(TEST_WORKSPACENAME)
            .featureServiceName(TEST_FEATURESERVICE_NAME)
            .requestDataList(defaultFeatureRequestDataList)
            .microBatchSize(8)
            .metadataOptions(MetadataOption.NAME, MetadataOption.SLO_INFO)
            .build();

    Assert.assertFalse(getFeaturesBatchRequest.isBatchRequest());
    Assert.assertEquals(1, getFeaturesBatchRequest.getRequestList().size());

    GetFeaturesRequest getFeaturesRequest =
        (GetFeaturesRequest) getFeaturesBatchRequest.getRequestList().get(0);
    checkGetFeaturesCommonFields(
        getFeaturesRequest,
        ENDPOINT,
        EnumSet.of(MetadataOption.NAME, MetadataOption.DATA_TYPE, MetadataOption.SLO_INFO));
  }

  @Test
  public void testBatchRequestWithTwoRequestData_shouldCallBatchEndpoint() {
    // GetFeaturesBatchRequest with two requestData and default microBatchSize should create 1
    // GetFeaturesMicroBatchRequest
    // with a requestDatalist of size 2
    GetFeaturesRequestData requestData = new GetFeaturesRequestData().addJoinKey("user", "123");
    getFeaturesBatchRequest =
        new GetFeaturesBatchRequest.Builder()
            .workspaceName(TEST_WORKSPACENAME)
            .featureServiceName(TEST_FEATURESERVICE_NAME)
            .requestDataList(defaultFeatureRequestDataList)
            .addRequestData(requestData)
            .metadataOptions(MetadataOption.NAME, MetadataOption.SLO_INFO)
            .build();

    Assert.assertTrue(getFeaturesBatchRequest.isBatchRequest());
    Assert.assertEquals(1, getFeaturesBatchRequest.getRequestList().size());

    GetFeaturesMicroBatchRequest microBatchRequest =
        (GetFeaturesMicroBatchRequest) getFeaturesBatchRequest.getRequestList().get(0);
    checkGetFeaturesCommonFields(
        microBatchRequest,
        BATCH_ENDPOINT,
        EnumSet.of(MetadataOption.NAME, MetadataOption.DATA_TYPE, MetadataOption.SLO_INFO));

    Assert.assertEquals(2, microBatchRequest.getFeaturesRequestDataList().size());
  }

  @Test
  public void testBatchRequestWithSixRequestData_shouldCallBatchEndpoint() {
    // GetFeaturesBatchRequest with 6 requests should create 2 GetFeaturesMicroBatchRequests
    // with a requestDatalist of size 5 and 1 respectively
    getFeaturesBatchRequest =
        new GetFeaturesBatchRequest.Builder()
            .workspaceName(TEST_WORKSPACENAME)
            .featureServiceName(TEST_FEATURESERVICE_NAME)
            .requestDataList(defaultFeatureRequestDataList)
            .requestDataList(generateRequestDataForSize(6))
            .metadataOptions(MetadataOption.NAME, MetadataOption.SLO_INFO)
            .build();

    Assert.assertTrue(getFeaturesBatchRequest.isBatchRequest());
    Assert.assertEquals(2, getFeaturesBatchRequest.getRequestList().size());

    List<GetFeaturesMicroBatchRequest> microBatchRequestList =
        (List<GetFeaturesMicroBatchRequest>) getFeaturesBatchRequest.getRequestList();
    Assert.assertEquals(5, microBatchRequestList.get(0).getFeaturesRequestDataList().size());
    Assert.assertEquals(1, microBatchRequestList.get(1).getFeaturesRequestDataList().size());
  }

  @Test
  public void testBatchRequestWithTwentyRequestData_shouldCallBatchEndpoint() {
    // GetFeaturesBatchRequest with 20 requests and microBatchSize of 8 should create 3
    // GetFeaturesMicroBatchRequests
    // with a requestDatalist of size 8,8 and 4 respectively
    getFeaturesBatchRequest =
        new GetFeaturesBatchRequest.Builder()
            .workspaceName(TEST_WORKSPACENAME)
            .featureServiceName(TEST_FEATURESERVICE_NAME)
            .requestDataList(defaultFeatureRequestDataList)
            .requestDataList(generateRequestDataForSize(20))
            .microBatchSize(8)
            .metadataOptions(MetadataOption.NAME, MetadataOption.SLO_INFO)
            .build();

    Assert.assertTrue(getFeaturesBatchRequest.isBatchRequest());
    Assert.assertEquals(3, getFeaturesBatchRequest.getRequestList().size());

    List<GetFeaturesMicroBatchRequest> microBatchRequestList =
        (List<GetFeaturesMicroBatchRequest>) getFeaturesBatchRequest.getRequestList();
    Assert.assertEquals(8, microBatchRequestList.get(0).getFeaturesRequestDataList().size());
    Assert.assertEquals(8, microBatchRequestList.get(1).getFeaturesRequestDataList().size());
    Assert.assertEquals(4, microBatchRequestList.get(2).getFeaturesRequestDataList().size());
  }

  @Test
  public void testBatchRequestWithMicroBatchSizeOne_shouldCallGetFeaturesEndpoint() {
    // GetFeaturesBatchRequest with 20 requests and microBatchSize of 1 should create 20 individual
    // GetFeaturesRequests
    getFeaturesBatchRequest =
        new GetFeaturesBatchRequest.Builder()
            .workspaceName(TEST_WORKSPACENAME)
            .featureServiceName(TEST_FEATURESERVICE_NAME)
            .requestDataList(defaultFeatureRequestDataList)
            .requestDataList(generateRequestDataForSize(20))
            .microBatchSize(1)
            .metadataOptions(MetadataOption.NAME, MetadataOption.SLO_INFO)
            .build();

    Assert.assertFalse(getFeaturesBatchRequest.isBatchRequest());
    Assert.assertEquals(20, getFeaturesBatchRequest.getRequestList().size());
  }

  @Test
  public void testGivenBatchRequestObject_shouldSerializeToValidString() throws IOException {

    getFeaturesBatchRequest =
        new GetFeaturesBatchRequest.Builder()
            .requestDataList(generateRequestDataFromFile())
            .workspaceName("prod")
            .featureServiceName("fraud_detection_feature_service")
            .metadataOptions(MetadataOption.ALL)
            .build();

    String expected_json =
        "{\"params\":{\"feature_service_name\":\"fraud_detection_feature_service\",\"metadata_options\":{\"include_slo_info\":true,\"include_effective_times\":true,\"include_names\":true,\"include_data_types\":true},\"request_data\":[{\"join_key_map\":{\"user_id\":\"user_656020174537\",\"merchant\":\"fraud_Cummerata-Jones\"},\"request_context_map\":{\"amt\":61.06}},{\"join_key_map\":{\"user_id\":\"user_394495759023\",\"merchant\":\"fraud_Marks Inc\"},\"request_context_map\":{\"amt\":106.43}},{\"join_key_map\":{\"user_id\":\"user_656020174537\",\"merchant\":\"fraud_Grimes LLC\"},\"request_context_map\":{\"amt\":24.95}},{\"join_key_map\":{\"user_id\":\"user_499975010057\",\"merchant\":\"fraud_Thiel Ltd\"},\"request_context_map\":{\"amt\":2.12}},{\"join_key_map\":{\"user_id\":\"user_656020174537\",\"merchant\":\"fraud_Bins-Rice\"},\"request_context_map\":{\"amt\":68.31}}],\"workspace_name\":\"prod\"}}";
    Assert.assertTrue(getFeaturesBatchRequest.isBatchRequest());
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

  private List<GetFeaturesRequestData> generateRequestDataForSize(int size) {
    List<GetFeaturesRequestData> requestDataList = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      String key = RandomStringUtils.randomAlphanumeric(5);
      String val = RandomStringUtils.randomAlphanumeric(5);
      requestDataList.add(new GetFeaturesRequestData().addJoinKey(key, val));
    }
    return requestDataList;
  }

  private List<GetFeaturesRequestData> generateRequestDataFromFile() throws IOException {
    List<GetFeaturesRequestData> requestDataList = new ArrayList<>();
    File file = new File(getClass().getClassLoader().getResource("request/input.csv").getFile());
    String content = new String(Files.readAllBytes(file.toPath()));
    Arrays.asList(StringUtils.split(content, "\n"))
        .forEach(
            row -> {
              String[] vals = StringUtils.split(row, ",");
              requestDataList.add(
                  new GetFeaturesRequestData()
                      .addJoinKey("user_id", vals[0])
                      .addJoinKey("merchant", vals[2])
                      .addRequestContext("amt", Double.parseDouble(vals[1])));
            });
    return requestDataList;
  }
}
