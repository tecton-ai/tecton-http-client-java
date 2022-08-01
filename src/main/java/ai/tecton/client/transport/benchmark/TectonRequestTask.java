package ai.tecton.client.transport.benchmark;

import ai.tecton.client.request.AbstractTectonRequest;
import ai.tecton.client.response.GetFeaturesResponse;
import ai.tecton.client.transport.HttpResponse;
import ai.tecton.client.transport.TectonHttpClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

class TectonRequestTask extends TimerTask {

  static AtomicInteger REQUEST_COUNTER = new AtomicInteger(0);

  List<SingleCallMetrics> callMetricsList;
  TectonHttpClient tectonHttpClient;
  AbstractTectonRequest request;

  TectonRequestTask(TectonHttpClient tectonHttpClient, AbstractTectonRequest request) {
    this.tectonHttpClient = tectonHttpClient;
    this.callMetricsList = new ArrayList<>();
    this.request = request;
  }

  @Override
  public void run() {
    // Setup
    REQUEST_COUNTER.getAndIncrement();
    boolean isSuccessful = false;
    long start = System.currentTimeMillis();

    // Perform request
    HttpResponse httpResponse =
        tectonHttpClient.performRequest(
            request.getEndpoint(), request.getMethod(), request.requestToJson());
    long responseStart = System.currentTimeMillis();
    if (httpResponse.isSuccessful()) {
      GetFeaturesResponse response =
          new GetFeaturesResponse(
              httpResponse.getResponseBody().get(), httpResponse.getRequestDuration());
      isSuccessful = true;
    }
    long stop = System.currentTimeMillis();

    // Add call metrics to list
    Optional<HttpMetrics> httpMetrics = httpResponse.getCallMetrics();
    if (httpMetrics.isPresent()) {
      callMetricsList.add(
          new SingleCallMetrics(
              httpMetrics.get(), (stop - start), (stop - responseStart), isSuccessful));
    }
  }
}
