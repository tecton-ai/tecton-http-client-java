package ai.tecton.client.transport.benchmark;

// Metrics for a single Client call
class SingleCallMetrics {

  HttpMetrics httpMetrics;
  Long totalDuration;
  Long clientLatency;
  boolean isSuccessful;

  SingleCallMetrics(
      HttpMetrics httpMetrics, Long totalDuration, Long clientLatency, boolean isSuccessful) {
    this.httpMetrics = httpMetrics;
    this.totalDuration = totalDuration;
    this.clientLatency = clientLatency;
    this.isSuccessful = isSuccessful;
  }
}
