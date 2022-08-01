package ai.tecton.client.transport.benchmark;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

class BenchmarkMetrics {

  private Metric okHttpCallDuration;
  private Metric requestHeaderDuration;
  private Metric requestBodyDuration;
  private Metric responseHeaderDuration;
  private Metric responseBodyDuration;
  private Metric featureServiceResponseLatency;
  private Metric clientLatency;
  private long queriesPerSecond;
  private Double percentagePassed;
  private Double percentageFailed;
  long totalRequests;

  DecimalFormat formatter = new DecimalFormat("#0.00");

  BenchmarkMetrics(List<CallMetrics> clientMetrics, long durationInSeconds) {
    // Collect metrics from all calls
    long durationInMillis = TimeUnit.SECONDS.toMillis(durationInSeconds);
    int totalCalls = clientMetrics.size();
    this.setOkHttpCallDuration(
            clientMetrics.stream()
                .map(CallMetrics::getTotalCallDuration)
                .collect(Collectors.toList()))
        .setRequestHeaderDuration(
            clientMetrics.stream()
                .map(CallMetrics::getRequestHeaderDuration)
                .collect(Collectors.toList()))
        .setRequestBodyDuration(
            clientMetrics.stream()
                .map(CallMetrics::getRequestBodyDuration)
                .collect(Collectors.toList()))
        .setResponseHeaderDuration(
            clientMetrics.stream()
                .map(CallMetrics::getResponseHeaderDuration)
                .collect(Collectors.toList()))
        .setResponseBodyDuration(
            clientMetrics.stream()
                .map(CallMetrics::getResponseBodyDuration)
                .collect(Collectors.toList()))
        .setFsLatency(
            clientMetrics.stream()
                .map(CallMetrics::getResponseLatency)
                .collect(Collectors.toList()))
        .setClientLatency(
            clientMetrics.stream().map(CallMetrics::getClientLatency).collect(Collectors.toList()))
        .setPercentages(
            clientMetrics.stream().filter(CallMetrics::isSuccessful).count(),
            clientMetrics.stream().filter(m -> !m.isSuccessful()).count())
        .setQueriesPerSecond(totalCalls, TimeUnit.MILLISECONDS.toSeconds(durationInMillis));
  }

  BenchmarkMetrics setOkHttpCallDuration(List<Long> okHttpCallDurationList) {
    this.okHttpCallDuration =
        createMetric(
            okHttpCallDurationList,
            "OkHttp Call Duration",
            "Time between when a call is first initiated and the call is ended by the OkHttp client");
    return this;
  }

  BenchmarkMetrics setRequestHeaderDuration(List<Long> requestHeaderDuration) {
    this.requestHeaderDuration =
        createMetric(
            requestHeaderDuration,
            "OkHttp Request Header Duration",
            "Time between sending the first and last byte of request headers by the client");
    return this;
  }

  BenchmarkMetrics setRequestBodyDuration(List<Long> requestBodyDuration) {
    this.requestBodyDuration =
        createMetric(
            requestBodyDuration,
            "OkHttp Request Body Duration",
            "Time between receiving the first and last response header bytes from the server");
    return this;
  }

  BenchmarkMetrics setResponseHeaderDuration(List<Long> responseHeaderDurationList) {
    this.responseHeaderDuration =
        createMetric(
            responseHeaderDurationList,
            "Receive Response Header Duration",
            "Time between reading the first and last response header bytes from the server");
    return this;
  }

  BenchmarkMetrics setResponseBodyDuration(List<Long> responseBodyDurationList) {
    this.responseBodyDuration =
        createMetric(
            responseBodyDurationList,
            "Receive Response Body Duration",
            "Time between reading the first and last response body bytes from the server");
    return this;
  }

  BenchmarkMetrics setFsLatency(List<Long> fsLatencyList) {
    this.featureServiceResponseLatency =
        createMetric(
            fsLatencyList,
            "HTTP API Response Latency",
            "Time between when the last request body byte was sent and the first response header byte was received. This includes the online store latency as well as the network latency.");
    return this;
  }

  BenchmarkMetrics setClientLatency(List<Long> clientLatencyList) {
    this.clientLatency =
        createMetric(clientLatencyList, "Client Latency", "Client Response Parsing Time");
    return this;
  }

  BenchmarkMetrics setQueriesPerSecond(int totalQueries, long totalDurationInSeconds) {
    this.queriesPerSecond = totalQueries / totalDurationInSeconds;
    return this;
  }

  BenchmarkMetrics setPercentages(long totalPassed, long totalFailed) {
    this.totalRequests = totalFailed + totalPassed;
    this.percentageFailed = totalFailed * 100.0 / (totalPassed + totalFailed);
    this.percentagePassed = totalPassed * 100.0 / (totalPassed + totalFailed);
    return this;
  }

  private Metric createMetric(List<Long> values, String name, String description) {
    // Calculate average, p95 and p99
    Metric metric = new Metric();
    metric.values = new ArrayList<>(values);
    Collections.sort(values);
    metric.average = values.stream().mapToLong(a -> a).average().getAsDouble();
    metric.p95 = percentile(values, 95);
    metric.p99 = percentile(values, 99);
    metric.name = name;
    metric.description = description;
    return metric;
  }

  class Metric {
    List<Long> values;
    String name;
    String description;
    Double average;
    Long p95;
    Long p99;
  }

  static long percentile(List<Long> latencies, double percentile) {
    int index = (int) Math.ceil(percentile / 100.0 * latencies.size());
    return latencies.get(index - 1);
  }

  void print() {
    System.out.println("\n-----------------SUMMARY---------------------\n\n");
    System.out.println("Total Number of Requests: " + totalRequests);
    System.out.println("% of successful requests: " + percentagePassed);
    System.out.println("% of failed requests: " + percentageFailed);
    System.out.println("Actual QPS: " + queriesPerSecond);

    printMetric(requestHeaderDuration);
    printMetric(requestBodyDuration);
    printMetric(responseHeaderDuration);
    printMetric(responseBodyDuration);
    printMetric(featureServiceResponseLatency);
    printMetric(okHttpCallDuration);
    printMetric(clientLatency);
  }

  private void printMetric(Metric metric) {
    System.out.println("--------------------------------------");
    System.out.println("Metric: " + metric.name);
    System.out.println("Description: " + metric.description);
    System.out.println("--------------------------------------");
    System.out.println("\nAverage: " + formatter.format(metric.average) + " ms");
    System.out.println("P95: " + metric.p95 + " ms");
    System.out.println("P99: " + metric.p99 + " ms");
  }
}
