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

public class GetFeaturesRequestTest {

  private static final String TEST_WORKSPACENAME = "testWorkspaceName";
  private static final String TEST_FEATURESERVICE_NAME = "testFSName";
  private static final String ENDPOINT = "/api/v1/feature-service/get-features";
  private static final Set<MetadataOption> defaultMetadataOptions =
      EnumSet.of(MetadataOption.NAME, MetadataOption.DATA_TYPE);

  GetFeaturesRequest getFeaturesRequest;
  GetFeaturesRequestData defaultFeatureRequestData;
  ClassLoader classLoader;

  @Before
  public void setup() {
    defaultFeatureRequestData = new GetFeaturesRequestData();
    defaultFeatureRequestData.addJoinKey("testKey", "testValue");
    classLoader = getClass().getClassLoader();
  }

  @Test
  public void testEmptyWorkspaceName() {
    try {
      getFeaturesRequest =
          new GetFeaturesRequest("", TEST_FEATURESERVICE_NAME, defaultFeatureRequestData);
      fail();
    } catch (TectonClientException e) {
      Assert.assertEquals(TectonErrorMessage.INVALID_WORKSPACENAME, e.getMessage());
    }
  }

  @Test
  public void testEmptyFeatureServiceName() {
    try {
      getFeaturesRequest =
          new GetFeaturesRequest(TEST_WORKSPACENAME, "", defaultFeatureRequestData);
      fail();
    } catch (TectonClientException e) {
      Assert.assertEquals(TectonErrorMessage.INVALID_FEATURESERVICENAME, e.getMessage());
    }
  }

  @Test
  public void testNullWorkspaceName() {
    try {
      getFeaturesRequest =
          new GetFeaturesRequest(null, TEST_FEATURESERVICE_NAME, defaultFeatureRequestData);
      fail();
    } catch (TectonClientException e) {
      Assert.assertEquals(TectonErrorMessage.INVALID_WORKSPACENAME, e.getMessage());
    }
  }

  @Test
  public void testNullFeatureServiceName() {
    try {
      getFeaturesRequest =
          new GetFeaturesRequest(TEST_WORKSPACENAME, null, defaultFeatureRequestData);
      fail();
    } catch (TectonClientException e) {
      Assert.assertEquals(TectonErrorMessage.INVALID_FEATURESERVICENAME, e.getMessage());
    }
  }

  @Test
  public void testEmptyMaps() {
    GetFeaturesRequestData getFeaturesRequestData = new GetFeaturesRequestData();
    try {
      getFeaturesRequest =
          new GetFeaturesRequest(
              TEST_WORKSPACENAME, TEST_FEATURESERVICE_NAME, getFeaturesRequestData);
      fail();
    } catch (TectonClientException e) {
      Assert.assertEquals(TectonErrorMessage.EMPTY_REQUEST_MAPS, e.getMessage());
    }
  }

  @Test
  public void testSimpleRequest() {
    getFeaturesRequest =
        new GetFeaturesRequest(
            TEST_WORKSPACENAME, TEST_FEATURESERVICE_NAME, defaultFeatureRequestData);

    Assert.assertEquals(ENDPOINT, getFeaturesRequest.getEndpoint());
    Assert.assertEquals(TectonHttpClient.HttpMethod.POST, getFeaturesRequest.getMethod());
    Assert.assertEquals(TEST_WORKSPACENAME, getFeaturesRequest.getWorkspaceName());
    Assert.assertEquals(TEST_FEATURESERVICE_NAME, getFeaturesRequest.getFeatureServiceName());
    Assert.assertTrue(getFeaturesRequest.getFeaturesRequestData().isEmptyRequestContextMap());
    Assert.assertEquals(defaultMetadataOptions, getFeaturesRequest.getMetadataOptions());

    Map<String, String> joinKeyMap = getFeaturesRequest.getFeaturesRequestData().getJoinKeyMap();
    Assert.assertEquals(1, joinKeyMap.size());
    Assert.assertEquals("testValue", joinKeyMap.get("testKey"));

    String expected_json =
        "{\"params\":{\"feature_service_name\":\"testFSName\",\"join_key_map\":{\"testKey\":\"testValue\"},\"metadata_options\":{\"include_names\":true,\"include_data_types\":true},\"workspace_name\":\"testWorkspaceName\"}}";
    String actual_json = getFeaturesRequest.requestToJson();

    Assert.assertEquals(expected_json, actual_json);
  }

  @Test
  public void testRequestWithRequestContextMap() throws IOException {
    defaultFeatureRequestData.addRequestContext("testKey1", 999.999);
    defaultFeatureRequestData.addRequestContext("testKey2", "testVal");

    getFeaturesRequest =
        new GetFeaturesRequest(
            TEST_WORKSPACENAME, TEST_FEATURESERVICE_NAME, defaultFeatureRequestData);
    Assert.assertEquals(ENDPOINT, getFeaturesRequest.getEndpoint());
    Assert.assertEquals(TectonHttpClient.HttpMethod.POST, getFeaturesRequest.getMethod());
    Assert.assertEquals(TEST_WORKSPACENAME, getFeaturesRequest.getWorkspaceName());
    Assert.assertEquals(TEST_FEATURESERVICE_NAME, getFeaturesRequest.getFeatureServiceName());
    Assert.assertEquals(defaultMetadataOptions, getFeaturesRequest.getMetadataOptions());

    Map<String, Object> requestContextMap =
        getFeaturesRequest.getFeaturesRequestData().getRequestContextMap();
    Assert.assertEquals(2, requestContextMap.size());
    Assert.assertEquals(999.999, requestContextMap.get("testKey1"));
    Assert.assertEquals("testVal", requestContextMap.get("testKey2"));

    String expected_json =
        "{\"params\":{\"feature_service_name\":\"testFSName\",\"join_key_map\":{\"testKey\":\"testValue\"},\"metadata_options\":{\"include_names\":true,\"include_data_types\":true},\"request_context_map\":{\"testKey2\":\"testVal\",\"testKey1\":999.999},\"workspace_name\":\"testWorkspaceName\"}}";
    String actual_json = getFeaturesRequest.requestToJson();

    Assert.assertEquals(expected_json, actual_json);
  }

  @Test
  public void testAllMetadataOptions() {
    getFeaturesRequest =
        new GetFeaturesRequest(
            TEST_WORKSPACENAME,
            TEST_FEATURESERVICE_NAME,
            defaultFeatureRequestData,
            RequestConstants.ALL_METADATA_OPTIONS);
    Assert.assertEquals(4, getFeaturesRequest.getMetadataOptions().size());
    Set<MetadataOption> metadataOptionSet = getFeaturesRequest.getMetadataOptions();
    Set<MetadataOption> expectedSet =
        new HashSet<>(
            Arrays.asList(
                MetadataOption.NAME,
                MetadataOption.DATA_TYPE,
                MetadataOption.EFFECTIVE_TIME,
                MetadataOption.SLO_INFO));
    Assert.assertTrue(metadataOptionSet.containsAll(expectedSet));
  }

  @Test
  public void testCustomMetadataOptions() {
    getFeaturesRequest =
        new GetFeaturesRequest(
            TEST_WORKSPACENAME,
            TEST_FEATURESERVICE_NAME,
            defaultFeatureRequestData,
            RequestConstants.DEFAULT_METADATA_OPTIONS);
    Assert.assertEquals(2, getFeaturesRequest.getMetadataOptions().size());
    Set<MetadataOption> metadataOptionSet = getFeaturesRequest.getMetadataOptions();
    Set<MetadataOption> expectedSet =
        new HashSet<>(Arrays.asList(MetadataOption.NAME, MetadataOption.DATA_TYPE));
    Assert.assertTrue(metadataOptionSet.containsAll(expectedSet));
  }

  @Test
  public void testJsonWithCustomMetadataOptions() {
    defaultFeatureRequestData.addRequestContext("testKey", 999.99);
    getFeaturesRequest =
        new GetFeaturesRequest(
            TEST_WORKSPACENAME,
            TEST_FEATURESERVICE_NAME,
            defaultFeatureRequestData,
            new HashSet<>(Arrays.asList(MetadataOption.NAME, MetadataOption.SLO_INFO)));

    Assert.assertEquals(3, getFeaturesRequest.getMetadataOptions().size());

    String expected_json =
        "{\"params\":{"
            + "\"feature_service_name\":\"testFSName\","
            + "\"join_key_map\":{"
            + "\"testKey\":\"testValue\""
            + "},"
            + "\"metadata_options\":{"
            + "\"include_slo_info\":true,"
            + "\"include_names\":true,"
            + "\"include_data_types\":true},"
            + "\"request_context_map\":{"
            + "\"testKey\":999.99},"
            + "\"workspace_name\":\"testWorkspaceName\""
            + "}}";
    String actual_json = getFeaturesRequest.requestToJson();
    Assert.assertEquals(expected_json, actual_json);
  }

  @Test
  public void testJsonWithAllMetadataOptions() {
    defaultFeatureRequestData.addRequestContext("testKey", "testValue");
    getFeaturesRequest =
        new GetFeaturesRequest(
            TEST_WORKSPACENAME,
            TEST_FEATURESERVICE_NAME,
            defaultFeatureRequestData,
            RequestConstants.ALL_METADATA_OPTIONS);

    Assert.assertEquals(4, getFeaturesRequest.getMetadataOptions().size());

    String expected_json =
        "{\"params\":{"
            + "\"feature_service_name\":\"testFSName\","
            + "\"join_key_map\":{"
            + "\"testKey\":\"testValue\""
            + "},"
            + "\"metadata_options\":{"
            + "\"include_slo_info\":true,"
            + "\"include_effective_times\":true,"
            + "\"include_names\":true,"
            + "\"include_data_types\":true"
            + "},"
            + "\"request_context_map\":{"
            + "\"testKey\":\"testValue\""
            + "},"
            + "\"workspace_name\":\"testWorkspaceName\""
            + "}}";
    String actual_json = getFeaturesRequest.requestToJson();
    Assert.assertEquals(expected_json, actual_json);
  }
}
