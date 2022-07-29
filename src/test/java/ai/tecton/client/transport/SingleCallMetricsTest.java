package ai.tecton.client.transport;

import ai.tecton.client.request.GetFeaturesRequest;
import ai.tecton.client.request.GetFeaturesRequestData;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

class SingleCallMetricsTest {
  private TectonHttpClient tectonHttpClient;
  private final String WORKSPACE_NAME = "prod";
  private final String FEATURE_SERVICE_NAME = "fraud_detection_feature_service";
  String tectonUrl;
  String tectonApiKey;
  GetFeaturesRequest request;

  @Before
  public void setup() {
    // String tectonUrl = System.getenv("TECTON_BASE_URL");
    // String tectonApiKey = System.getenv("TECTON_API_KEY");

    tectonUrl = "https://app.tecton.ai";
    tectonApiKey = "ef171f52af035f189a8661ec658c6777";

    Assume.assumeFalse(StringUtils.isEmpty(tectonUrl));
    Assume.assumeFalse(StringUtils.isEmpty(tectonApiKey));

    tectonHttpClient = new TectonHttpClient(tectonUrl, tectonApiKey, true);
    GetFeaturesRequestData requestData =
        new GetFeaturesRequestData()
            .addJoinKey("user_id", "user_205125746682")
            .addJoinKey("merchant", "entertainment")
            .addRequestContext("amt", 500.00);
    request = new GetFeaturesRequest(WORKSPACE_NAME, FEATURE_SERVICE_NAME, requestData);
  }

  @Test
  public void testSingleCallGetFeatures() throws InterruptedException {

    SingleClientBenchmark clientBenchmark = new SingleClientBenchmark(tectonUrl, tectonApiKey);
    BenchmarkMetrics metrics = clientBenchmark.runTest(20, 5, request);
    metrics.print();
  }

  @Test
  public void testOneCall() {
    for (int i = 0; i < 5; i++) {
      HttpResponse httpResponse =
          tectonHttpClient.performRequest(
              request.getEndpoint(), request.getMethod(), request.requestToJson());
      System.out.println("----------------------------");
    }
  }
}
