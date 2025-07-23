package ai.tecton.client.request;

import static org.junit.Assert.*;

import ai.tecton.client.model.MetadataOption;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;

public class GetFeaturesRequestWithOptionsTest {

  @Test
  public void testGetFeaturesRequestWithRequestOptions() {
    // Create request data
    GetFeaturesRequestData requestData =
        new GetFeaturesRequestData.Builder()
            .joinKeyMap(java.util.Collections.singletonMap("testKey", "testValue"))
            .requestContextMap(java.util.Collections.singletonMap("testKey", "testValue"))
            .build();

    // Create request options
    RequestOptions requestOptions =
        new RequestOptions.Builder()
            .option("latency_budget_ms", 5000)
            .option("coerceNullCountsToZero", true)
            .option("readFromCache", false)
            .option("writeFromCache", true)
            .option("ignoreExtraRequestContextFields", false)
            .build();

    // Create request with options
    Set<MetadataOption> metadataOptions = new HashSet<>();
    metadataOptions.add(MetadataOption.NAME);
    metadataOptions.add(MetadataOption.DATA_TYPE);

    GetFeaturesRequest request =
        new GetFeaturesRequest(
            "testWorkspaceName", "testFSName", requestData, metadataOptions, requestOptions);

    // Verify JSON contains request_options with all expected values
    String json = request.requestToJson();
    assertTrue(json.contains("request_options"));
    assertTrue(json.contains("\"latency_budget_ms\":5000"));
    assertTrue(json.contains("\"coerceNullCountsToZero\":true"));
    assertTrue(json.contains("\"readFromCache\":false"));
    assertTrue(json.contains("\"writeFromCache\":true"));
    assertTrue(json.contains("\"ignoreExtraRequestContextFields\":false"));
  }

  @Test
  public void testGetFeaturesRequestBuilderWithRequestOptions() {
    // Create request data
    GetFeaturesRequestData requestData =
        new GetFeaturesRequestData.Builder()
            .joinKeyMap(java.util.Collections.singletonMap("testKey", "testValue"))
            .build();

    // Create request options
    RequestOptions requestOptions =
        new RequestOptions.Builder()
            .option("latency_budget_ms", 3000)
            .option("coerceNullCountsToZero", false)
            .option("readFromCache", true)
            .build();

    // Use the builder to create request with options
    GetFeaturesRequest request =
        new GetFeaturesRequest.Builder()
            .workspaceName("testWorkspaceName")
            .featureServiceName("testFSName")
            .getFeaturesRequestData(requestData)
            .requestOptions(requestOptions)
            .build();

    // Verify JSON contains request_options with expected structure
    String json = request.requestToJson();
    assertTrue(json.contains("request_options"));
    assertTrue(json.contains("\"latency_budget_ms\":3000"));
    assertTrue(json.contains("\"coerceNullCountsToZero\":false"));
    assertTrue(json.contains("\"readFromCache\":true"));
  }

  @Test
  public void testGetFeaturesRequestWithoutRequestOptions() {
    // Create request data
    GetFeaturesRequestData requestData =
        new GetFeaturesRequestData.Builder()
            .joinKeyMap(java.util.Collections.singletonMap("testKey", "testValue"))
            .build();

    // Create request without options
    GetFeaturesRequest request =
        new GetFeaturesRequest("testWorkspaceName", "testFSName", requestData);

    // Verify JSON does not contain request_options
    String json = request.requestToJson();
    assertFalse(json.contains("request_options"));
  }
}
