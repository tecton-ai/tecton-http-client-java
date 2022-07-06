package com.tecton.client.request;

import com.tecton.client.transport.TectonHttpClient;
import org.junit.Assert;
import org.junit.Test;

public class GetFeatureServiceMetadataRequestTest {

  private static final String TEST_WORKSPACENAME = "testWorkspaceName";
  private static final String TEST_FEATURESERVICE_NAME = "testFSName";
  private static final String ENDPOINT = "/api/v1/feature-service/metadata";
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
  }
}
