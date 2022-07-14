package com.tecton.client.transport;

import com.tecton.client.request.GetFeatureServiceMetadataRequest;
import com.tecton.client.request.GetFeaturesRequest;
import com.tecton.client.request.GetFeaturesRequestData;
import com.tecton.client.response.GetFeaturesResponse;
import com.tecton.client.transport.TectonHttpClient.HttpMethod;
import java.time.Duration;
import java.util.stream.IntStream;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

public class SingleCallMetricsTest {
  private TectonHttpClient tectonHttpClient;
  private final HttpMethod method = HttpMethod.POST;
  private final String WORKSPACE_NAME = "prod";
  private final String FEATURE_SERVICE_NAME = "fraud_detection_feature_service";

  @Before
  public void setup() {
    // String tectonUrl = System.getenv("TECTON_BASE_URL");
    // String tectonApiKey = System.getenv("TECTON_API_KEY");

    String tectonUrl = "https://app.tecton.ai";
    String tectonApiKey = "ef171f52af035f189a8661ec658c6777";

    Assume.assumeFalse(StringUtils.isEmpty(tectonUrl));
    Assume.assumeFalse(StringUtils.isEmpty(tectonApiKey));

    tectonHttpClient = new TectonHttpClient(tectonUrl, tectonApiKey, true);

    // Warm-up before perf tests
    GetFeatureServiceMetadataRequest request =
        new GetFeatureServiceMetadataRequest("fraud_detection_feature_service", "prod");
    IntStream.rangeClosed(1, 10)
        .forEach(i -> performSingleCall(request.getEndpoint(), request.requestToJson()));
  }

  @Test
  public void testSingleCallGetFeatures() {
    GetFeaturesRequestData requestData =
        new GetFeaturesRequestData()
            .addJoinKey("user_id", "user_205125746682")
            .addJoinKey("merchant", "entertainment")
            .addRequestContext("amt", 500.00);
    GetFeaturesRequest request =
        new GetFeaturesRequest(WORKSPACE_NAME, FEATURE_SERVICE_NAME, requestData);
    long requestStartTime = System.currentTimeMillis();
    HttpResponse response = performSingleCall(request.getEndpoint(), request.requestToJson());
    GetFeaturesResponse getFeaturesResponse =
        new GetFeaturesResponse(response.getResponseBody().get(), response.getRequestDuration());
    long requestStopTime = System.currentTimeMillis();

    Duration totalHttpCallDuration = Duration.ofMillis(requestStopTime - requestStartTime);
    HttpMetrics metrics = response.getCallMetrics();

    long totalTimeClient = totalHttpCallDuration.toMillis();
    long okhttpCallDuration = metrics.getTotalCallDuration().toMillis();
    long responseBodyDuration = metrics.getResponseBodyDuration().toNanos();
    long requestBodyDuration = metrics.getRequestBodyDuration().toMillis();
    long successfulConnection = metrics.getSuccessfulConnectionTime().toMillis();
  }

  private HttpResponse performSingleCall(String endpoint, String requestBody) {
    return tectonHttpClient.performRequest(endpoint, method, requestBody);
  }
}
