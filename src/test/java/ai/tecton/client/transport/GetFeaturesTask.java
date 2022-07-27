package ai.tecton.client.transport;

import ai.tecton.client.request.GetFeaturesRequest;
import ai.tecton.client.response.GetFeaturesResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

class GetFeaturesTask extends TimerTask {

  static AtomicInteger REQUEST_COUNTER = new AtomicInteger(0);

  List<CallMetrics> callMetricsList;
  TectonHttpClient tectonHttpClient;
  long targetTime;
  GetFeaturesRequest request;

  GetFeaturesTask(long targetTime, TectonHttpClient tectonHttpClient, GetFeaturesRequest request) {
    this.tectonHttpClient = tectonHttpClient;
    this.targetTime = targetTime;
    this.callMetricsList = new ArrayList<>();
    this.request = request;
  }

  @Override
  public void run() {
    REQUEST_COUNTER.getAndIncrement();
    boolean isSuccessful = false;
    long start = System.currentTimeMillis();
    HttpResponse httpResponse =
        tectonHttpClient.performRequest(
            request.getEndpoint(), request.getMethod(), request.requestToJson());
    long clientStart = System.currentTimeMillis();
    if (httpResponse.isSuccessful()) {
      GetFeaturesResponse response =
          new GetFeaturesResponse(
              httpResponse.getResponseBody().get(), httpResponse.getRequestDuration());
      isSuccessful = true;
    }
    long stop = System.currentTimeMillis();
    long totalClientDuration = stop - start;
    long clientResponseParsingDuration = stop - clientStart;
    callMetricsList.add(
        new CallMetrics(
            httpResponse.getCallMetrics(),
            totalClientDuration,
            clientResponseParsingDuration,
            isSuccessful));
  }
}
