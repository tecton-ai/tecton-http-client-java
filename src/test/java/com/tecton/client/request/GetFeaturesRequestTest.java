package com.tecton.client.request;

import static org.junit.Assert.fail;

import com.tecton.client.exceptions.TectonClientException;
import com.tecton.client.exceptions.TectonErrorMessage;
import com.tecton.client.request.GetFeaturesRequest.MetadataOption;
import com.tecton.client.transport.TectonHttpClient;
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
  ClassLoader classLoader;

  @Before
  public void setup() {
    classLoader = getClass().getClassLoader();
  }

  @Test
  public void testEmptyWorkspaceName() {
    try {
      getFeaturesRequest =
          new GetFeaturesRequest.Builder()
              .workspaceName("")
              .featureServiceName(TEST_FEATURESERVICE_NAME)
              .build();
      fail();
    } catch (IllegalArgumentException e) {
      Assert.assertEquals(TectonErrorMessage.INVALID_WORKSPACENAME, e.getMessage());
    }
  }

  @Test
  public void testEmptyFeatureServiceName() {
    try {
      getFeaturesRequest =
          new GetFeaturesRequest.Builder()
              .workspaceName(TEST_WORKSPACENAME)
              .featureServiceName("")
              .build();
      fail();
    } catch (IllegalArgumentException e) {
      Assert.assertEquals(TectonErrorMessage.INVALID_FEATURESERVICENAME, e.getMessage());
    }
  }

  @Test
  public void testNullWorkspaceName() {
    try {
      getFeaturesRequest =
          new GetFeaturesRequest.Builder()
              .workspaceName(null)
              .featureServiceName(TEST_FEATURESERVICE_NAME)
              .build();
      fail();
    } catch (NullPointerException e) {
      Assert.assertEquals(TectonErrorMessage.INVALID_WORKSPACENAME, e.getMessage());
    }
  }

  @Test
  public void testNullFeatureServiceName() {
    try {
      getFeaturesRequest =
          new GetFeaturesRequest.Builder()
              .workspaceName(TEST_WORKSPACENAME)
              .featureServiceName(null)
              .build();
      fail();
    } catch (NullPointerException e) {
      Assert.assertEquals(TectonErrorMessage.INVALID_FEATURESERVICENAME, e.getMessage());
    }
  }

  @Test
  public void testEmptyMaps() {
    try {
      getFeaturesRequest =
          new GetFeaturesRequest.Builder()
              .workspaceName(TEST_WORKSPACENAME)
              .featureServiceName(TEST_FEATURESERVICE_NAME)
              .joinKeyMap(new HashMap<>())
              .requestContextMap(new HashMap<>())
              .build();
      fail();
    } catch (TectonClientException e) {
      Assert.assertEquals(TectonErrorMessage.EMPTY_REQUEST_MAPS, e.getMessage());
    }
  }

  @Test
  public void testNullJoinKey() {
    try {
      getFeaturesRequest =
          new GetFeaturesRequest.Builder()
              .workspaceName(TEST_WORKSPACENAME)
              .featureServiceName(TEST_FEATURESERVICE_NAME)
              .joinKey(null, "testValue")
              .build();
      fail();
    } catch (NullPointerException e) {
      Assert.assertEquals(TectonErrorMessage.INVALID_KEY_VALUE, e.getMessage());
    }
  }

  @Test
  public void testNullJoinValue() {
    try {
      getFeaturesRequest =
          new GetFeaturesRequest.Builder()
              .workspaceName(TEST_WORKSPACENAME)
              .featureServiceName(TEST_FEATURESERVICE_NAME)
              .joinKey("testKey", (String) null)
              .build();
      fail();
    } catch (NullPointerException e) {
      Assert.assertEquals(TectonErrorMessage.INVALID_KEY_VALUE, e.getMessage());
    }
  }

  @Test
  public void testNullRequestContextKey() {
    try {
      getFeaturesRequest =
          new GetFeaturesRequest.Builder()
              .workspaceName(TEST_WORKSPACENAME)
              .featureServiceName(TEST_FEATURESERVICE_NAME)
              .requestContext(null, "testValue")
              .build();
      fail();
    } catch (NullPointerException e) {
      Assert.assertEquals(TectonErrorMessage.INVALID_KEY_VALUE, e.getMessage());
    }
  }

  @Test
  public void testNullRequestContextValue() {
    try {
      getFeaturesRequest =
          new GetFeaturesRequest.Builder()
              .workspaceName(TEST_WORKSPACENAME)
              .featureServiceName(TEST_FEATURESERVICE_NAME)
              .requestContext("testKey", (String) null)
              .build();
      fail();
    } catch (NullPointerException e) {
      Assert.assertEquals(TectonErrorMessage.INVALID_KEY_VALUE, e.getMessage());
    }
  }

  @Test
  public void testEmptyJoinKey() {
    try {
      getFeaturesRequest =
          new GetFeaturesRequest.Builder()
              .workspaceName(TEST_WORKSPACENAME)
              .featureServiceName(TEST_FEATURESERVICE_NAME)
              .joinKey("", "testValue")
              .build();
      fail();
    } catch (IllegalArgumentException e) {
      Assert.assertEquals(TectonErrorMessage.INVALID_KEY_VALUE, e.getMessage());
    }
  }

  @Test
  public void testEmptyJoinValue() {
    try {
      getFeaturesRequest =
          new GetFeaturesRequest.Builder()
              .workspaceName(TEST_WORKSPACENAME)
              .featureServiceName(TEST_FEATURESERVICE_NAME)
              .joinKey("testKey", "")
              .build();
      fail();
    } catch (IllegalArgumentException e) {
      Assert.assertEquals(TectonErrorMessage.INVALID_KEY_VALUE, e.getMessage());
    }
  }

  @Test
  public void testEmptyRequestContextKey() {
    try {
      getFeaturesRequest =
          new GetFeaturesRequest.Builder()
              .workspaceName(TEST_WORKSPACENAME)
              .featureServiceName(TEST_FEATURESERVICE_NAME)
              .requestContext("", "testValue")
              .build();
      fail();
    } catch (IllegalArgumentException e) {
      Assert.assertEquals(TectonErrorMessage.INVALID_KEY_VALUE, e.getMessage());
    }
  }

  @Test
  public void testEmptyRequestContextValue() {
    try {
      getFeaturesRequest =
          new GetFeaturesRequest.Builder()
              .workspaceName(TEST_WORKSPACENAME)
              .featureServiceName(TEST_FEATURESERVICE_NAME)
              .requestContext("testKey", "")
              .build();
      fail();
    } catch (IllegalArgumentException e) {
      Assert.assertEquals(TectonErrorMessage.INVALID_KEY_VALUE, e.getMessage());
    }
  }

  @Test
  public void testMixedTypeJoinKeyValues() {
    getFeaturesRequest =
        new GetFeaturesRequest.Builder()
            .workspaceName(TEST_WORKSPACENAME)
            .featureServiceName(TEST_FEATURESERVICE_NAME)
            .joinKey("testStringKey", "testValue")
            .joinKey("testLongKey", 1234L)
            .build();

    Map<String, String> joinKeyMap = getFeaturesRequest.getJoinKeyMap();
    Assert.assertNotNull(joinKeyMap);
    Assert.assertEquals(2, joinKeyMap.size());
    Assert.assertEquals(joinKeyMap.get("testStringKey"), "testValue");
    Assert.assertEquals(joinKeyMap.get("testLongKey"), "1234");
  }

  @Test
  public void testMixedTypeRequestContextKeyValues() {
    getFeaturesRequest =
        new GetFeaturesRequest.Builder()
            .workspaceName(TEST_WORKSPACENAME)
            .featureServiceName(TEST_FEATURESERVICE_NAME)
            .requestContext("testStringKey", "testStringValue")
            .requestContext("testLongKey", 1234L)
            .requestContext("testDoubleKey", 125.55)
            .build();

    Map<String, Object> requestContextMap = getFeaturesRequest.getRequestContextMap();
    Assert.assertNotNull(requestContextMap);
    Assert.assertEquals(3, requestContextMap.size());
    Assert.assertEquals(requestContextMap.get("testStringKey"), "testStringValue");
    Assert.assertEquals(requestContextMap.get("testLongKey"), "1234");
    Assert.assertEquals(requestContextMap.get("testDoubleKey"), 125.55);
  }

  @Test
  public void testJoinKeyAndRequestContext() {
    Map<String, String> joinKeyMap =
        new HashMap<String, String>() {
          {
            put("testJoinKey1", "testJoinValue1");
            put("testJoinKey2", "testJoinValue2");
          }
        };

    Map<String, Object> requestContextMap =
        new HashMap<String, Object>() {
          {
            put("testRequestContext1", 555.55);
            put("testRequestContext2", "testStringValue");
          }
        };

    getFeaturesRequest =
        new GetFeaturesRequest.Builder()
            .workspaceName(TEST_WORKSPACENAME)
            .featureServiceName(TEST_FEATURESERVICE_NAME)
            .joinKeyMap(joinKeyMap)
            .requestContextMap(requestContextMap)
            .build();

    Assert.assertEquals(2, getFeaturesRequest.getRequestContextMap().size());
    Assert.assertEquals(2, getFeaturesRequest.getJoinKeyMap().size());
    joinKeyMap
        .keySet()
        .forEach(
            key -> {
              Assert.assertEquals(joinKeyMap.get(key), getFeaturesRequest.getJoinKeyMap().get(key));
            });

    requestContextMap
        .keySet()
        .forEach(
            key -> {
              Assert.assertEquals(
                  requestContextMap.get(key), getFeaturesRequest.getRequestContextMap().get(key));
            });
  }

  @Test
  public void testSimpleRequest() {
    getFeaturesRequest =
        new GetFeaturesRequest.Builder()
            .workspaceName(TEST_WORKSPACENAME)
            .featureServiceName(TEST_FEATURESERVICE_NAME)
            .joinKey("testKey", "testValue")
            .build();

    Assert.assertEquals(ENDPOINT, getFeaturesRequest.getEndpoint());
    Assert.assertEquals(TectonHttpClient.HttpMethod.POST, getFeaturesRequest.getMethod());
    Assert.assertEquals(TEST_WORKSPACENAME, getFeaturesRequest.getWorkspaceName());
    Assert.assertEquals(TEST_FEATURESERVICE_NAME, getFeaturesRequest.getFeatureServiceName());
    Assert.assertTrue(getFeaturesRequest.getRequestContextMap().isEmpty());
    Assert.assertEquals(defaultMetadataOptions, getFeaturesRequest.getMetadataOptions());

    Map<String, String> joinKeyMap = getFeaturesRequest.getJoinKeyMap();
    Assert.assertEquals(1, joinKeyMap.size());
    Assert.assertEquals("testValue", joinKeyMap.get("testKey"));

    String expected_json =
        "{\"params\":{\"feature_service_name\":\"testFSName\",\"join_key_map\":{\"testKey\":\"testValue\"},\"metadata_options\":{\"include_names\":true,\"include_data_types\":true},\"workspace_name\":\"testWorkspaceName\"}}";
    String actual_json = getFeaturesRequest.requestToJson();

    Assert.assertEquals(expected_json, actual_json);
  }

  @Test
  public void testRequestWithRequestContextMap() throws IOException {

    getFeaturesRequest =
        new GetFeaturesRequest.Builder()
            .workspaceName(TEST_WORKSPACENAME)
            .featureServiceName(TEST_FEATURESERVICE_NAME)
            .joinKey("testKey", "testValue")
            .requestContext("testKey1", 999.999)
            .requestContext("testKey2", "testVal")
            .build();

    Assert.assertEquals(ENDPOINT, getFeaturesRequest.getEndpoint());
    Assert.assertEquals(TectonHttpClient.HttpMethod.POST, getFeaturesRequest.getMethod());
    Assert.assertEquals(TEST_WORKSPACENAME, getFeaturesRequest.getWorkspaceName());
    Assert.assertEquals(TEST_FEATURESERVICE_NAME, getFeaturesRequest.getFeatureServiceName());
    Assert.assertEquals(defaultMetadataOptions, getFeaturesRequest.getMetadataOptions());

    Map<String, Object> requestContextMap = getFeaturesRequest.getRequestContextMap();
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
        new GetFeaturesRequest.Builder()
            .workspaceName(TEST_WORKSPACENAME)
            .featureServiceName(TEST_FEATURESERVICE_NAME)
            .joinKey("testKey", "testValue")
            .metadataOptions(MetadataOption.ALL)
            .build();
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
        new GetFeaturesRequest.Builder()
            .workspaceName(TEST_WORKSPACENAME)
            .featureServiceName(TEST_FEATURESERVICE_NAME)
            .joinKey("testKey", "testValue")
            .metadataOptions(MetadataOption.NAME, MetadataOption.DATA_TYPE)
            .build();
    Assert.assertEquals(2, getFeaturesRequest.getMetadataOptions().size());
    Set<MetadataOption> metadataOptionSet = getFeaturesRequest.getMetadataOptions();
    Set<MetadataOption> expectedSet =
        new HashSet<>(Arrays.asList(MetadataOption.NAME, MetadataOption.DATA_TYPE));
    Assert.assertTrue(metadataOptionSet.containsAll(expectedSet));
  }

  @Test
  public void testJsonWithCustomMetadataOptions() {
    getFeaturesRequest =
        new GetFeaturesRequest.Builder()
            .workspaceName(TEST_WORKSPACENAME)
            .featureServiceName(TEST_FEATURESERVICE_NAME)
            .joinKey("testKey", "testValue")
            .requestContext("testKey", 999.99)
            .metadataOptions(MetadataOption.NAME, MetadataOption.SLO_INFO)
            .build();

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
    getFeaturesRequest =
        new GetFeaturesRequest.Builder()
            .workspaceName(TEST_WORKSPACENAME)
            .featureServiceName(TEST_FEATURESERVICE_NAME)
            .joinKey("testKey", "testValue")
            .requestContext("testKey", "testValue")
            .metadataOptions(MetadataOption.ALL)
            .build();

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
