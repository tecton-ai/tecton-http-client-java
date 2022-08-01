package ai.tecton.client.transport.benchmark;

import ai.tecton.client.request.AbstractTectonRequest;
import ai.tecton.client.response.GetFeaturesResponse;
import ai.tecton.client.transport.HttpResponse;
import ai.tecton.client.transport.TectonHttpClient;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

class SingleClientWorker {

  private final TectonHttpClient tectonHttpClient;
  private static final int MAX_THREADS = 10;
  private static final int NUMBER_OF_POLLS = 5;
  private static final String THREAD = "Thread";
  private static final int QPS_MARGIN = 10;
  private static final int BASELINE_QPS = 20;
  private static final int PERIOD = 3;

  SingleClientWorker(TectonHttpClient tectonHttpClient) {
    this.tectonHttpClient = tectonHttpClient;
  }

  List<CallMetrics> runTest(
      int qps, long durationInSeconds, AbstractTectonRequest request, int qpsPerClient)
      throws InterruptedException {
    List<CallMetrics> allMetrics = new ArrayList<>();
    long durationInMillis = TimeUnit.SECONDS.toMillis(durationInSeconds) + QPS_MARGIN;
    warmup(request);

    // Initialize thread pool with a starting number of threads, with name and task
    int numberOfThreads = Math.max(qpsPerClient / BASELINE_QPS, 1);
    Map<String, Pair<Timer, TectonRequestTask>> threadToTaskPool = new HashMap<>();
    for (int i = 0; i < numberOfThreads; i++) {
      String name = StringUtils.join(THREAD, i);
      threadToTaskPool.put(
          name, Pair.of(new Timer("Thread" + i), new TectonRequestTask(tectonHttpClient, request)));
    }

    // Run all threads in the pool
    long splitDurationInMillis = durationInMillis / NUMBER_OF_POLLS;
    long start = System.currentTimeMillis();
    threadToTaskPool
        .values()
        .forEach(
            threadTaskPair ->
                threadTaskPair.getKey().scheduleAtFixedRate(threadTaskPair.getRight(), 0, PERIOD));

    // Poll qps NUMBER_OF_POLLS times and adjust thread pool if necessary
    for (int i = 0; i < NUMBER_OF_POLLS; i++) {
      Thread.sleep(splitDurationInMillis);
      long current = System.currentTimeMillis();
      long currentQps =
          TectonRequestTask.REQUEST_COUNTER.get()
              / TimeUnit.MILLISECONDS.toSeconds(current - start);

      if (currentQps < (qps - QPS_MARGIN) && threadToTaskPool.size() < MAX_THREADS) {
        // If qps is low and number of threads < MAX_THREADS, add thread to pool
        String name = StringUtils.join(THREAD, numberOfThreads);
        numberOfThreads++;
        Timer timer = new Timer(name);
        TectonRequestTask task = new TectonRequestTask(tectonHttpClient, request);
        timer.scheduleAtFixedRate(task, 0, PERIOD);
        threadToTaskPool.put(name, Pair.of(timer, task));

      } else if (currentQps > qps && threadToTaskPool.size() > 1) {
        // if QPS is high and pool has > 1 threads, stop a thread
        String name = StringUtils.join(THREAD, (numberOfThreads - 1));
        Pair<Timer, TectonRequestTask> removedThread = threadToTaskPool.remove(name);
        removedThread.getKey().cancel();
        allMetrics.addAll(removedThread.getValue().callMetricsList);
        numberOfThreads--;
      }
    }

    // Stop all threads
    threadToTaskPool.values().forEach(timerPair -> timerPair.getKey().cancel());

    // Collect metrics from all threads in a client
    List<TectonRequestTask> allTasks =
        threadToTaskPool.values().stream().map(Pair::getValue).collect(Collectors.toList());
    allMetrics.addAll(
        allTasks.stream()
            .map(v -> v.callMetricsList)
            .flatMap(List::stream)
            .collect(Collectors.toList()));
    return allMetrics;
  }

  // Warmup calls before running the tests
  private void warmup(AbstractTectonRequest request) {
    for (int i = 0; i < 20; i++) {
      HttpResponse httpResponse =
          tectonHttpClient.performRequest(
              request.getEndpoint(), request.getMethod(), request.requestToJson());
      if (httpResponse.isSuccessful()) {
        GetFeaturesResponse getFeaturesResponse =
            new GetFeaturesResponse(
                httpResponse.getResponseBody().get(), httpResponse.getRequestDuration());
      }
    }
  }
}
