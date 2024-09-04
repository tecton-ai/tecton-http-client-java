package ai.tecton.client.request;

import static org.junit.Assert.fail;
import ai.tecton.client.exceptions.InvalidRequestParameterException;
import ai.tecton.client.exceptions.TectonErrorMessage;
import ai.tecton.client.model.MetadataOption;
import ai.tecton.client.transport.TectonHttpClient;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
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
    } catch (InvalidRequestParameterException e) {
      Assert.assertEquals(TectonErrorMessage.INVALID_WORKSPACENAME, e.getMessage());
    }
  }

  @Test
  public void testEmptyFeatureServiceName() {
    try {
      getFeaturesRequest =
          new GetFeaturesRequest(TEST_WORKSPACENAME, "", defaultFeatureRequestData);
      fail();
    } catch (InvalidRequestParameterException e) {
      Assert.assertEquals(TectonErrorMessage.INVALID_FEATURESERVICENAME, e.getMessage());
    }
  }

  @Test
  public void testNullWorkspaceName() {
    try {
      getFeaturesRequest =
          new GetFeaturesRequest(null, TEST_FEATURESERVICE_NAME, defaultFeatureRequestData);
      fail();
    } catch (InvalidRequestParameterException e) {
      Assert.assertEquals(TectonErrorMessage.INVALID_WORKSPACENAME, e.getMessage());
    }
  }

  @Test
  public void testNullFeatureServiceName() {
    try {
      getFeaturesRequest =
          new GetFeaturesRequest(TEST_WORKSPACENAME, null, defaultFeatureRequestData);
      fail();
    } catch (InvalidRequestParameterException e) {
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
    } catch (InvalidRequestParameterException e) {
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
        "{\"params\":{\"feature_service_name\":\"testFSName\",\"join_key_map\":{\"testKey\":\"testValue\"},\"metadata_options\":{\"include_names\":true,\"include_data_types\":true},\"request_context_map\":null,\"workspace_name\":\"testWorkspaceName\"}}";
    String actual_json = getFeaturesRequest.requestToJson();

    Assert.assertEquals(expected_json, actual_json);
  }

  @Test
  public void testSimpleRequestWithNullJoinKey() {
    defaultFeatureRequestData.addJoinKey("testNullKey", (String) null);
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
    Assert.assertEquals(2, joinKeyMap.size());
    Assert.assertEquals("testValue", joinKeyMap.get("testKey"));

    String expected_json =
        "{\"params\":{\"feature_service_name\":\"testFSName\",\"join_key_map\":{\"testNullKey\":null,\"testKey\":\"testValue\"},\"metadata_options\":{\"include_names\":true,\"include_data_types\":true},\"request_context_map\":null,\"workspace_name\":\"testWorkspaceName\"}}";
    String actual_json = getFeaturesRequest.requestToJson();

    Assert.assertEquals(expected_json, actual_json);
  }

  @Test
  public void testRequestWithRequestContextMap() throws IOException {
    defaultFeatureRequestData.addRequestContext("testKey1", 999.999);
    defaultFeatureRequestData.addRequestContext("testKey2", "testVal");
    defaultFeatureRequestData.addRequestContext("testNullKey", (String) null);

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
    Assert.assertEquals(3, requestContextMap.size());
    Assert.assertEquals(999.999, requestContextMap.get("testKey1"));
    Assert.assertEquals("testVal", requestContextMap.get("testKey2"));
    Assert.assertNull(requestContextMap.get("testNullKey"));

    String expected_json =
        "{\"params\":{\"feature_service_name\":\"testFSName\",\"join_key_map\":{\"testKey\":\"testValue\"},\"metadata_options\":{\"include_names\":true,\"include_data_types\":true},\"request_context_map\":{\"testKey2\":\"testVal\",\"testNullKey\":null,\"testKey1\":999.999},\"workspace_name\":\"testWorkspaceName\"}}";
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
    Assert.assertEquals(7, getFeaturesRequest.getMetadataOptions().size());
    Set<MetadataOption> metadataOptionSet = getFeaturesRequest.getMetadataOptions();
    Set<MetadataOption> expectedSet =
        new HashSet<>(
            Arrays.asList(
                MetadataOption.NAME,
                MetadataOption.DATA_TYPE,
                MetadataOption.EFFECTIVE_TIME,
                MetadataOption.SLO_INFO,
                MetadataOption.FEATURE_STATUS,
                MetadataOption.FEATURE_DESCRIPTION,
                MetadataOption.FEATURE_TAGS));
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
  public void testJsonWithAllMetadataOptions() throws IOException {
    defaultFeatureRequestData.addRequestContext("testKey", "testValue");
    getFeaturesRequest =
        new GetFeaturesRequest(
            TEST_WORKSPACENAME,
            TEST_FEATURESERVICE_NAME,
            defaultFeatureRequestData,
            RequestConstants.ALL_METADATA_OPTIONS);

    Assert.assertEquals(7, getFeaturesRequest.getMetadataOptions().size());

    String expected_json =
        "{\"params\":{"
            + "\"feature_service_name\":\"testFSName\","
            + "\"join_key_map\":{"
            + "\"testKey\":\"testValue\""
            + "},"
            + "\"metadata_options\":{"
            + "\"include_feature_descriptions\":true,"
            + "\"include_feature_tags\":true,"
            + "\"include_slo_info\":true,"
            + "\"include_effective_times\":true,"
            + "\"include_names\":true,"
            + "\"include_data_types\":true,"
            + "\"include_serving_status\":true"
            + "},"
            + "\"request_context_map\":{"
            + "\"testKey\":\"testValue\""
            + "},"
            + "\"workspace_name\":\"testWorkspaceName\""
            + "}}";

    String actual_json = getFeaturesRequest.requestToJson();
    Moshi moshi = new Moshi.Builder().build();
    Type type = Types.newParameterizedType(Map.class, String.class, Object.class);
    JsonAdapter<Map<String, Object>> adapter = moshi.adapter(type);

    Map<String, Object> expectedMap = adapter.fromJson(expected_json);
    Map<String, Object> actualMap = adapter.fromJson(actual_json);
    Assert.assertEquals(expectedMap, actualMap);
  }

  @Test
  public void testEqualsAndHashCode() {
    GetFeaturesRequest getFeaturesRequest =
        new GetFeaturesRequest(
            TEST_WORKSPACENAME, TEST_FEATURESERVICE_NAME, defaultFeatureRequestData);

    GetFeaturesRequest getFeaturesRequestEquals =
        new GetFeaturesRequest.Builder()
            .workspaceName(TEST_WORKSPACENAME)
            .featureServiceName(TEST_FEATURESERVICE_NAME)
            .getFeaturesRequestData(defaultFeatureRequestData)
            .build();

    GetFeaturesRequest getFeaturesRequestNotEquals =
        new GetFeaturesRequest.Builder()
            .workspaceName("dev_workspace")
            .featureServiceName(TEST_FEATURESERVICE_NAME)
            .getFeaturesRequestData(defaultFeatureRequestData)
            .build();

    Assert.assertEquals(getFeaturesRequest, getFeaturesRequestEquals);
    Assert.assertEquals(getFeaturesRequest.hashCode(), getFeaturesRequestEquals.hashCode());

    Assert.assertNotEquals(getFeaturesRequest, getFeaturesRequestNotEquals);
    Assert.assertNotEquals(getFeaturesRequest.hashCode(), getFeaturesRequestNotEquals.hashCode());
  }
}
