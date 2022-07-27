package ai.tecton.client.transport;

class CallMetrics {

  HttpMetrics httpMetrics;
  Long totalDuration;
  Long clientResponseParsingDuration;
  boolean isSuccessful;

  CallMetrics(
      HttpMetrics httpMetrics,
      Long totalDuration,
      Long clientResponseParsingDuration,
      boolean isSuccessful) {
    this.httpMetrics = httpMetrics;
    this.totalDuration = totalDuration;
    this.clientResponseParsingDuration = clientResponseParsingDuration;
    this.isSuccessful = isSuccessful;
  }
}
