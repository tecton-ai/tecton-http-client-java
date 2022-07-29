package ai.tecton.client.transport;

import ai.tecton.client.request.GetFeaturesRequest;
import ai.tecton.client.response.GetFeaturesResponse;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assume;

class SingleClientBenchmark {

  private final TectonHttpClient tectonHttpClient;
  private final int BASELINE_QPS = 15;
  private final int MAX_THREADS = 10;
  private static final int MARGIN = 3;
  private static final String THREAD = "Thread";

  SingleClientBenchmark(String tectonUrl, String tectonApiKey) {

    tectonUrl = "https://app.tecton.ai";
    tectonApiKey = "ef171f52af035f189a8661ec658c6777";

    Assume.assumeFalse(StringUtils.isEmpty(tectonUrl));
    Assume.assumeFalse(StringUtils.isEmpty(tectonApiKey));
    tectonHttpClient = new TectonHttpClient(tectonUrl, tectonApiKey, true);
  }

  BenchmarkMetrics runTest(int qps, long durationInSeconds, GetFeaturesRequest request)
      throws InterruptedException {
    List<CallMetrics> allMetrics = new ArrayList<>();
    long durationInMillis = TimeUnit.SECONDS.toMillis(durationInSeconds);
    warmup(request);

    // Initialize thread pool with a starting number of threads, with name and task
    int numberOfThreads = qps / BASELINE_QPS;
    Map<String, Pair<Timer, GetFeaturesTask>> threadToTaskPool = new HashMap<>();
    for (int i = 0; i < numberOfThreads; i++) {
      String name = StringUtils.join(THREAD, i);
      threadToTaskPool.put(
          name, Pair.of(new Timer("Thread" + i), new GetFeaturesTask(tectonHttpClient, request)));
    }

    // Run all threads in the pool
    long splitDurationInMillis = durationInMillis / 3;
    long start = System.currentTimeMillis();
    threadToTaskPool
        .values()
        .forEach(
            threadTaskPair ->
                threadTaskPair.getKey().scheduleAtFixedRate(threadTaskPair.getRight(), 0, 1));

    // Check qps every 1/3rd of total duration and adjust thread pool
    for (int i = 0; i < 3; i++) {
      Thread.sleep(splitDurationInMillis);
      if (threadToTaskPool.size() >= MAX_THREADS || threadToTaskPool.size() == 1) continue;

      long current = System.currentTimeMillis();
      long currentQps =
          GetFeaturesTask.REQUEST_COUNTER.get() / TimeUnit.MILLISECONDS.toSeconds(current - start);
      if (currentQps > qps) {
        // If current QPS is high, remove thread from the pool
        String name = StringUtils.join(THREAD, (numberOfThreads - 1));
        Pair<Timer, GetFeaturesTask> removedThread = threadToTaskPool.remove(name);
        removedThread.getKey().cancel();
        allMetrics.addAll(removedThread.getValue().callMetricsList);
        numberOfThreads--;

      } else if (current < qps - MARGIN) {
        // if current qps is low, add thread to the pool
        String name = StringUtils.join(THREAD, numberOfThreads);
        numberOfThreads++;
        Timer timer = new Timer(name);
        GetFeaturesTask task = new GetFeaturesTask(tectonHttpClient, request);
        timer.scheduleAtFixedRate(task, 0, 1);
        threadToTaskPool.put(name, Pair.of(timer, task));
      }
    }

    // Stop all threads
    threadToTaskPool.values().forEach(timerPair -> timerPair.getKey().cancel());
    long stop = System.currentTimeMillis();

    // Collect metrics from all threads
    List<GetFeaturesTask> allTasks =
        threadToTaskPool.values().stream().map(Pair::getValue).collect(Collectors.toList());
    allMetrics.addAll(
        allTasks.stream()
            .map(v -> v.callMetricsList)
            .flatMap(List::stream)
            .collect(Collectors.toList()));
    return new BenchmarkMetrics(allMetrics, (stop - start));
  }

  private void warmup(GetFeaturesRequest request) {
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
