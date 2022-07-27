package ai.tecton.client.transport;

public class HttpMetrics {
  private long totalCallDuration;
  private long requestHeaderDuration;
  private long requestBodyDuration;
  private long responseHeaderDuration;
  private long responseBodyDuration;
  private long responseLatency;
  private long connectionAttemptTime;
  private long successfulConnectionTime;

  HttpMetrics setTotalCallDuration(long totalCallDuration) {
    this.totalCallDuration = totalCallDuration;
    return this;
  }

  HttpMetrics setRequestHeaderDuration(long requestHeaderDuration) {
    this.requestHeaderDuration = requestHeaderDuration;
    return this;
  }

  HttpMetrics setRequestBodyDuration(long requestBodyDuration) {
    this.requestBodyDuration = requestBodyDuration;
    return this;
  }

  HttpMetrics setResponseHeaderDuration(long responseHeaderDuration) {
    this.responseHeaderDuration = responseHeaderDuration;
    return this;
  }

  HttpMetrics setResponseBodyDuration(long responseBodyDuration) {
    this.responseBodyDuration = responseBodyDuration;
    return this;
  }

  HttpMetrics setResponseLatency(long responseLatency) {
    this.responseLatency = responseLatency;
    return this;
  }

  HttpMetrics setConnectionAttemptTime(long connectionAttemptTime) {
    this.connectionAttemptTime = connectionAttemptTime;
    return this;
  }

  HttpMetrics setSuccessfulConnectionTime(long successfulConnectionTime) {
    this.successfulConnectionTime = successfulConnectionTime;
    return this;
  }

  long getTotalCallDuration() {
    return totalCallDuration;
  }

  long getResponseHeaderDuration() {
    return responseHeaderDuration;
  }

  long getResponseBodyDuration() {
    return responseBodyDuration;
  }

  long getRequestBodyDuration() {
    return requestBodyDuration;
  }

  long getRequestHeaderDuration() {
    return requestHeaderDuration;
  }

  long getResponseLatency() {
    return responseLatency;
  }

  long getConnectionAttemptTime() {
    return this.connectionAttemptTime;
  }

  long getSuccessfulConnectionTime() {
    return this.successfulConnectionTime;
  }
}
