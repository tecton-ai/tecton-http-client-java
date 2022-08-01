package ai.tecton.client.transport.benchmark;

import ai.tecton.client.request.AbstractTectonRequest;
import ai.tecton.client.request.GetFeaturesRequest;
import ai.tecton.client.request.GetFeaturesRequestData;
import ai.tecton.client.transport.TectonHttpClient;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class BenchMarkRunner {

  private static final String WORKSPACE_NAME = "prod";
  private static final String FEATURE_SERVICE_NAME = "fraud_detection_feature_service";
  private static final int MAX_USERS = 5;
  private static final int MAX_QPS = 120;
  private static final long MAX_DURATION_SECONDS = TimeUnit.MINUTES.toSeconds(2);

  public static void main(String[] args) throws InterruptedException {
    if (args.length < 5) {
      System.out.println(
          "MISSING ARGUMENTS!! Please provide all required arguments in the order - URL, ApiKey, Number of Users, QPS and Test Duration");
      System.exit(1);
    }

    // Get arguments
    String tectonUrl = args[0];
    String tectonApiKey = args[1];
    int numClients = Integer.parseInt(args[2]);
    int qps = Integer.parseInt(args[3]);
    long durationInSeconds = Long.parseLong(args[4]);

    checkArguments(numClients, qps, durationInSeconds);

    // Create GetFeaturesRequest for benchmark calls
    GetFeaturesRequestData requestData =
        new GetFeaturesRequestData()
            .addJoinKey("user_id", "user_205125746682")
            .addJoinKey("merchant", "entertainment")
            .addRequestContext("amt", 500.00);
    GetFeaturesRequest request =
        new GetFeaturesRequest(WORKSPACE_NAME, FEATURE_SERVICE_NAME, requestData);

    System.out.println(
        String.format(
            "\nRunning Benchmark Tests with %s users and %s QPS for %s seconds",
            numClients, qps, durationInSeconds));

    // Create clients
    ExecutorService executorService = Executors.newFixedThreadPool(100);
    List<ClientTask> clientTasks = new ArrayList<>(numClients);
    int qpsPerClient = qps / numClients;
    for (int i = 0; i < numClients; i++) {
      clientTasks.add(
          new ClientTask(
              i + 1, tectonUrl, tectonApiKey, request, qps, durationInSeconds, qpsPerClient));
    }

    // Run tests for all clients
    clientTasks.forEach(executorService::execute);
    executorService.shutdown();
    executorService.awaitTermination(durationInSeconds + 10, TimeUnit.SECONDS);

    // Collect metrics from each client, aggregate and print
    List<CallMetrics> callMetricsList =
        clientTasks.stream()
            .map(t -> t.clientMetrics)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());

    BenchmarkMetrics benchmarkMetrics = new BenchmarkMetrics(callMetricsList, durationInSeconds);
    benchmarkMetrics.print();
  }

  private static void checkArguments(int numClients, int qps, long duration) {
    if (numClients > MAX_USERS) {
      System.out.println("Number of Users is greater than the max value " + MAX_USERS);
      System.exit(1);
    }
    if (qps > MAX_QPS) {
      System.out.println("QPS is greater than the max value " + MAX_QPS);
      System.exit(1);
    }
    if (duration > MAX_DURATION_SECONDS) {
      System.out.println("Test Duration is greater than the max value " + MAX_USERS);
      System.exit(1);
    }
  }
}

// Runnable thread per Client
class ClientTask implements Runnable {

  private final int id;
  private final SingleClientWorker clientWorker;
  TectonHttpClient tectonHttpClient;
  List<CallMetrics> clientMetrics;
  int qps;
  int qpsPerClient;
  long duration;
  AbstractTectonRequest request;

  ClientTask(
      int id,
      String url,
      String apiKey,
      AbstractTectonRequest request,
      int qps,
      long duration,
      int qpsPerClient) {
    this.id = id;
    tectonHttpClient = new TectonHttpClient(url, apiKey, true);
    clientWorker = new SingleClientWorker(tectonHttpClient);
    this.request = request;
    this.duration = duration;
    this.qps = qps;
    this.qpsPerClient = qpsPerClient;
  }

  public void run() {
    System.out.println("Client " + id + " : Start!");
    try {
      clientMetrics = clientWorker.runTest(qps, duration, request, qpsPerClient);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    System.out.println("Client " + id + " : Done!");
  }
}
