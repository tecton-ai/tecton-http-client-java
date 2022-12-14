package ai.tecton.client.request;

import ai.tecton.client.exceptions.TectonErrorMessage;
import ai.tecton.client.transport.TectonHttpClient;
import org.junit.Assert;
import org.junit.Test;

public class GetFeatureServiceMetadataRequestTest {

  private static final String TEST_WORKSPACENAME = "testWorkspaceName";
  private static final String TEST_FEATURESERVICE_NAME = "testFSName";
  private static final String ENDPOINT = "api/v1/feature-service/metadata";
  private static final String DEFAULT_WORKSPACE = "prod";

  GetFeatureServiceMetadataRequest getFeatureServiceMetadataRequest;

  @Test
  public void testMetadataRequestInDefaultWorkspace() {
    getFeatureServiceMetadataRequest =
        new GetFeatureServiceMetadataRequest(TEST_FEATURESERVICE_NAME);
    Assert.assertEquals(
        TEST_FEATURESERVICE_NAME, getFeatureServiceMetadataRequest.getFeatureServiceName());
    Assert.assertEquals(DEFAULT_WORKSPACE, getFeatureServiceMetadataRequest.getWorkspaceName());
    Assert.assertEquals(ENDPOINT, getFeatureServiceMetadataRequest.getEndpoint());
    Assert.assertEquals(
        TectonHttpClient.HttpMethod.POST, getFeatureServiceMetadataRequest.getMethod());
    String expectedRequest =
        "{\"params\":{\"feature_service_name\":\"testFSName\",\"workspace_name\":\"prod\"}}";
    Assert.assertEquals(expectedRequest, getFeatureServiceMetadataRequest.requestToJson());
  }

  @Test
  public void testMetadataRequestWithWorkspace() {
    getFeatureServiceMetadataRequest =
        new GetFeatureServiceMetadataRequest(TEST_FEATURESERVICE_NAME, TEST_WORKSPACENAME);
    Assert.assertEquals(
        TEST_FEATURESERVICE_NAME, getFeatureServiceMetadataRequest.getFeatureServiceName());
    Assert.assertEquals(TEST_WORKSPACENAME, getFeatureServiceMetadataRequest.getWorkspaceName());
    Assert.assertEquals(ENDPOINT, getFeatureServiceMetadataRequest.getEndpoint());
    Assert.assertEquals(
        TectonHttpClient.HttpMethod.POST, getFeatureServiceMetadataRequest.getMethod());
    String expectedJson =
        "{\"params\":{\"feature_service_name\":\"testFSName\",\"workspace_name\":\"testWorkspaceName\"}}";
    Assert.assertEquals(expectedJson, getFeatureServiceMetadataRequest.requestToJson());
  }

  @Test
  public void testEmptyWorkspace() {
    try {
      getFeatureServiceMetadataRequest =
          new GetFeatureServiceMetadataRequest(TEST_FEATURESERVICE_NAME, "");
    } catch (Exception e) {
      Assert.assertEquals(TectonErrorMessage.INVALID_WORKSPACENAME, e.getMessage());
    }
  }

  @Test
  public void testNullFeatureServiceName() {
    try {
      getFeatureServiceMetadataRequest = new GetFeatureServiceMetadataRequest(null);
    } catch (Exception e) {
      Assert.assertEquals(TectonErrorMessage.INVALID_FEATURESERVICENAME, e.getMessage());
    }
  }
}
