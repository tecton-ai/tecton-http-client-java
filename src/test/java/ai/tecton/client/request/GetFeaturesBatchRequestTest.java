package ai.tecton.client.request;

import static org.junit.Assert.fail;

import ai.tecton.client.exceptions.TectonClientException;
import ai.tecton.client.exceptions.TectonErrorMessage;
import ai.tecton.client.model.MetadataOption;
import ai.tecton.client.transport.TectonHttpClient;
import java.io.IOException;
import java.util.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GetFeaturesBatchRequestTest {

  private static final String TEST_WORKSPACENAME = "testWorkspaceName";
  private static final String TEST_FEATURESERVICE_NAME = "testFSName";
  private static final String ENDPOINT = "/api/v1/feature-service/get-features-batch";
  private static final Set<MetadataOption> defaultMetadataOptions =
      EnumSet.of(MetadataOption.NAME, MetadataOption.DATA_TYPE);

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
  public void testEmptyWorkspaceName() {
    try {
      getFeaturesBatchRequest =
          new GetFeaturesBatchRequest("", TEST_FEATURESERVICE_NAME, defaultFeatureRequestDataList);
      fail();
    } catch (IllegalArgumentException e) {
      Assert.assertEquals(TectonErrorMessage.INVALID_WORKSPACENAME, e.getMessage());
    }
  }

  @Test
  public void testNullFeatureServiceName() {
    try {
      getFeaturesBatchRequest =
          new GetFeaturesBatchRequest(TEST_WORKSPACENAME, null, defaultFeatureRequestDataList);
      fail();
    } catch (NullPointerException e) {
      Assert.assertEquals(TectonErrorMessage.INVALID_FEATURESERVICENAME, e.getMessage());
    }
  }

  @Test
  public void testEmptyMaps() {
    List<GetFeaturesRequestData> requestData = new ArrayList<>();
    requestData.add(new GetFeaturesRequestData());
    try {
      getFeaturesBatchRequest =
          new GetFeaturesBatchRequest(TEST_WORKSPACENAME, TEST_FEATURESERVICE_NAME, requestData);
      fail();
    } catch (TectonClientException e) {
      Assert.assertEquals(TectonErrorMessage.EMPTY_REQUEST_MAPS, e.getMessage());
    }
  }

  @Test
  public void testValidMicroBatchSize() {
    getFeaturesBatchRequest =
        new GetFeaturesBatchRequest(
            TEST_WORKSPACENAME, TEST_FEATURESERVICE_NAME, defaultFeatureRequestDataList);
    getFeaturesBatchRequest.setMicroBatchSize(8);
    Assert.assertEquals(8, getFeaturesBatchRequest.getMicroBatchSize());
  }

  @Test
  public void testInvalidMicroBatchSize() {
    try {
      getFeaturesBatchRequest =
          new GetFeaturesBatchRequest(
              TEST_WORKSPACENAME, TEST_FEATURESERVICE_NAME, defaultFeatureRequestDataList);
      getFeaturesBatchRequest.setMicroBatchSize(20);
      fail();
    } catch (TectonClientException e) {
      Assert.assertEquals(
          String.format(TectonErrorMessage.EXCEEDS_MAX_BATCH_SIZE, 10), e.getMessage());
    }
  }

  @Test
  public void testSingleRequestData() {
    getFeaturesBatchRequest =
        new GetFeaturesBatchRequest(
            TEST_WORKSPACENAME, TEST_FEATURESERVICE_NAME, defaultFeatureRequestDataList);

    Assert.assertEquals(ENDPOINT, getFeaturesBatchRequest.getEndpoint());
    Assert.assertEquals(TectonHttpClient.HttpMethod.POST, getFeaturesBatchRequest.getMethod());
    Assert.assertEquals(TEST_WORKSPACENAME, getFeaturesBatchRequest.getWorkspaceName());
    Assert.assertEquals(TEST_FEATURESERVICE_NAME, getFeaturesBatchRequest.getFeatureServiceName());
    Assert.assertEquals(defaultMetadataOptions, getFeaturesBatchRequest.getMetadataOptions());
    Assert.assertEquals(1, getFeaturesBatchRequest.getFeaturesRequestDataList().size());
    String expected_json =
        "{\"params\":{\"feature_service_name\":\"testFSName\",\"metadata_options\":{\"include_names\":true,\"include_data_types\":true},\"request_data\":[{\"join_key_map\":{\"testKey\":\"testValue\"}}],\"workspace_name\":\"testWorkspaceName\"}}";
    String actual_json = getFeaturesBatchRequest.requestToJson();
    Assert.assertEquals(expected_json, actual_json);
  }

  @Test
  public void testMultipleRequestData() throws IOException {
    List<GetFeaturesRequestData> requestDataList = new ArrayList<>();
    requestDataList.add(
        new GetFeaturesRequestData()
            .addJoinKey("user_id", "76")
            .addJoinKey("merchant", "44")
            .addRequestContext("amt", 1000L));
    requestDataList.add(
        new GetFeaturesRequestData()
            .addJoinKey("user_id", "3")
            .addJoinKey("merchant", "16")
            .addRequestContext("amt", 1000L));
    requestDataList.add(
        new GetFeaturesRequestData()
            .addJoinKey("user_id", "5")
            .addJoinKey("merchant", "32")
            .addRequestContext("amt", 1000L));
    requestDataList.add(
        new GetFeaturesRequestData()
            .addJoinKey("user_id", "61")
            .addJoinKey("merchant", "36")
            .addRequestContext("amt", 1000L));

    getFeaturesBatchRequest =
        new GetFeaturesBatchRequest(
            "prod", "fraud_detection_feature_service", requestDataList, MetadataOption.ALL);

    String expected_json =
        "{\"params\":{\"feature_service_name\":\"fraud_detection_feature_service\","
            + "\"metadata_options\":{\"include_slo_info\":true,\"include_effective_times\":true,\"include_names\":true,\"include_data_types\":true},"
            + "\"request_data\":[{\"join_key_map\":{\"user_id\":\"76\",\"merchant\":\"44\"},"
            + "\"request_context_map\":{\"amt\":\"1000\"}},{\"join_key_map\":{\"user_id\":\"3\",\"merchant\":\"16\"},"
            + "\"request_context_map\":{\"amt\":\"1000\"}},{\"join_key_map\":{\"user_id\":\"5\",\"merchant\":\"32\"},"
            + "\"request_context_map\":{\"amt\":\"1000\"}},{\"join_key_map\":{\"user_id\":\"61\",\"merchant\":\"36\"},"
            + "\"request_context_map\":{\"amt\":\"1000\"}}],\"workspace_name\":\"prod\"}}";
    String actual_json = getFeaturesBatchRequest.requestToJson();
    Assert.assertEquals(expected_json, actual_json);
  }
}
