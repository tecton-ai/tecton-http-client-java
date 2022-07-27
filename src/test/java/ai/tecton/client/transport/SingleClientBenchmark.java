package ai.tecton.client.transport;

import ai.tecton.client.request.GetFeaturesRequest;
import ai.tecton.client.response.GetFeaturesResponse;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assume;

public class SingleClientBenchmark {

  private final TectonHttpClient tectonHttpClient;
  private final int BASELINE_QPS = 15;

  SingleClientBenchmark(String tectonUrl, String tectonApiKey) {

    tectonUrl = "https://app.tecton.ai";
    tectonApiKey = "ef171f52af035f189a8661ec658c6777";

    Assume.assumeFalse(StringUtils.isEmpty(tectonUrl));
    Assume.assumeFalse(StringUtils.isEmpty(tectonApiKey));
    tectonHttpClient = new TectonHttpClient(tectonUrl, tectonApiKey, true);
  }

  BenchmarkMetrics runTest(int qps, long durationInSeconds, GetFeaturesRequest request)
      throws InterruptedException {
    long durationInMillis = TimeUnit.SECONDS.toMillis(durationInSeconds);
    long targetTime = System.currentTimeMillis() + durationInMillis;
    warmup(request);

    // Initialize thread pool with a starting number of threads
    int numberOfThreads = qps / BASELINE_QPS;
    Map<Timer, GetFeaturesTask> threadToTaskPool = new HashMap<>();
    List<Timer> timerThreads = new ArrayList<>();
    List<GetFeaturesTask> tasks = new ArrayList<>();
    for (int i = 0; i < numberOfThreads; i++) {
      threadToTaskPool.put(
          new Timer("Thread" + i), new GetFeaturesTask(targetTime, tectonHttpClient, request));
    }

    long start = System.currentTimeMillis();
    threadToTaskPool.forEach((timerThread, task) -> timerThread.scheduleAtFixedRate(task, 0, 1));
    Thread.sleep(durationInMillis);
    threadToTaskPool.keySet().forEach(Timer::cancel);
    long stop = System.currentTimeMillis();
    List<CallMetrics> metricsSuperSet =
        threadToTaskPool.values().stream()
            .map(v -> v.callMetricsList)
            .flatMap(List::stream)
            .collect(Collectors.toList());
    return new BenchmarkMetrics(metricsSuperSet, (stop - start));
  }

  void warmup(GetFeaturesRequest request) {
    for (int i = 0; i < 20; i++) {
      HttpResponse httpResponse =
          tectonHttpClient.performRequest(
              request.getEndpoint(), request.getMethod(), request.requestToJson());
      if (httpResponse.isSuccessful()) {
        try {
          GetFeaturesResponse getFeaturesResponse =
              new GetFeaturesResponse(
                  httpResponse.getResponseBody().get(), httpResponse.getRequestDuration());
        } catch (Exception e) {
          System.out.println("Exception!!!\n");
          System.out.println(httpResponse.getResponseBody().get());
          System.exit(1);
        }
      }
    }
  }
}
