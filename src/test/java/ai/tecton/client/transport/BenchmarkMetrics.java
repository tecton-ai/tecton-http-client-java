package ai.tecton.client.transport;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

class BenchmarkMetrics {

  private Metric okHttpCallDuration;
  private Metric okHttpConnectionDuration;
  private Metric requestHeaderDuration;
  private Metric requestBodyDuration;
  private Metric responseHeaderDuration;
  private Metric responseBodyDuration;
  private Metric featureServiceResponseLatency;
  private Metric clientResponseParsingDuration;
  private Metric clientLatency;
  private long queriesPerSecond;
  private Double percentagePassed;
  private Double percentageFailed;
  long totalRequests;

  DecimalFormat formatter = new DecimalFormat("#0.00");

  BenchmarkMetrics(List<CallMetrics> clientMetrics, long durationInMillis) {
    int totalCalls = clientMetrics.size();
    this.setOkHttpCallDuration(
            clientMetrics.stream()
                .map(m -> m.httpMetrics.getTotalCallDuration())
                .collect(Collectors.toList()))
        .setOkHttpConnectionDuration(
            clientMetrics.stream()
                .map(m -> m.httpMetrics.getSuccessfulConnectionTime())
                .collect(Collectors.toList()))
        .setResponseHeaderDuration(
            clientMetrics.stream()
                .map(m -> m.httpMetrics.getResponseHeaderDuration())
                .collect(Collectors.toList()))
        .setResponseBodyDuration(
            clientMetrics.stream()
                .map(m -> m.httpMetrics.getResponseBodyDuration())
                .collect(Collectors.toList()))
        .setFsLatency(
            clientMetrics.stream()
                .map(m -> m.httpMetrics.getResponseLatency())
                .collect(Collectors.toList()))
        .setClientLatency(
            clientMetrics.stream().map(m -> m.totalDuration).collect(Collectors.toList()))
        .setClientResponseParsingDuration(
            clientMetrics.stream()
                .map(m -> m.clientResponseParsingDuration)
                .collect(Collectors.toList()))
        .setPercentages(
            clientMetrics.stream().filter(m -> m.isSuccessful).count(),
            clientMetrics.stream().filter(m -> !m.isSuccessful).count())
        .setQueriesPerSecond(totalCalls, TimeUnit.MILLISECONDS.toSeconds(durationInMillis));
  }

  BenchmarkMetrics setOkHttpCallDuration(List<Long> okHttpCallDurationList) {
    this.okHttpCallDuration =
        createMetric(
            okHttpCallDurationList,
            "OkHttp Call Duration",
            "Time between when a call is executed and the call is ended by the OkHttp client.");
    return this;
  }

  BenchmarkMetrics setOkHttpConnectionDuration(List<Long> okHttpConnectionDurationList) {
    this.okHttpConnectionDuration =
        createMetric(
            okHttpConnectionDurationList,
            "OkHttp Connection Duration",
            "Time between when a successful connection is acquired with the server and the connection is released for the call");
    return this;
  }

  BenchmarkMetrics setResponseHeaderDuration(List<Long> responseHeaderDurationList) {
    this.responseHeaderDuration =
        createMetric(
            responseHeaderDurationList,
            "Receive Response Header Duration",
            "Time between when the first and last response header bytes were read from the server");
    return this;
  }

  BenchmarkMetrics setResponseBodyDuration(List<Long> responseBodyDurationList) {
    this.responseBodyDuration =
        createMetric(
            responseBodyDurationList,
            "Receive Response Body Duration",
            "Time between when the first and last response body bytes were read from the server");
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
        createMetric(
            clientLatencyList,
            "Total Client Duration",
            "Time between when the client started performing the request and the client completed parsing the response");
    return this;
  }

  BenchmarkMetrics setClientResponseParsingDuration(List<Long> clientResponseParsingDurationList) {
    this.clientResponseParsingDuration =
        createMetric(
            clientResponseParsingDurationList,
            "Client Response Parsing Duration",
            "Time between when the client received the HTTP Response and the client completed parsing and processing it to a GetFeaturesResponse");
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

    Metric metric = new Metric();
    metric.values = new ArrayList<>(values);
    Collections.sort(values);
    metric.average = values.stream().mapToLong(a -> a).average().getAsDouble();
    metric.max = values.stream().mapToLong(a -> a).max().getAsLong();
    metric.min = values.stream().mapToLong(a -> a).min().getAsLong();
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
    Long max;
    Long min;
  }

  static long percentile(List<Long> latencies, double percentile) {
    int index = (int) Math.ceil(percentile / 100.0 * latencies.size());
    return latencies.get(index - 1);
  }

  void print() {
    System.out.println("Total Number of Requests: " + totalRequests);
    System.out.println("% of successful requests: " + percentagePassed);
    System.out.println("% of failed requests: " + percentageFailed);
    System.out.println("Actual QPS: " + queriesPerSecond);
    printMetric(okHttpCallDuration);
    printMetric(okHttpConnectionDuration);
    printMetric(responseHeaderDuration);
    printMetric(responseBodyDuration);
    printMetric(featureServiceResponseLatency);
    printMetric(clientLatency);
    printMetric(clientResponseParsingDuration);
  }

  private void printMetric(Metric metric) {
    System.out.println("------------------------");
    System.out.println("Metric: " + metric.name);
    System.out.println("Description: " + metric.description);
    System.out.println("\nAverage: " + formatter.format(metric.average) + " ms");
    System.out.println("Min: " + metric.min + " ms");
    System.out.println("Max: " + metric.max + " ms");
    System.out.println("P95: " + metric.p95 + " ms");
    System.out.println("P99: " + metric.p99 + " ms");
    // System.out.println("Values: " + metric.values.toString());
    System.out.println("------------------------");
  }
}
