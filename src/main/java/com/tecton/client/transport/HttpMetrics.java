package com.tecton.client.transport;

import java.time.Duration;

public class HttpMetrics {
  private Duration totalCallDuration;
  private Duration requestHeaderDuration;
  private Duration requestBodyDuration;
  private Duration responseHeaderDuration;
  private Duration responseBodyDuration;
  private Duration connectionAttemptTime;
  private Duration successfulConnectionTime;
  private Duration dnsTime;

  HttpMetrics setTotalCallDuration(long totalCallDuration) {
    this.totalCallDuration = Duration.ofMillis(totalCallDuration);
    return this;
  }

  HttpMetrics setRequestHeaderDuration(long requestHeaderDuration) {
    this.requestHeaderDuration = Duration.ofMillis(requestHeaderDuration);
    return this;
  }

  HttpMetrics setRequestBodyDuration(long requestBodyDuration) {
    this.requestBodyDuration = Duration.ofMillis(requestBodyDuration);
    return this;
  }

  HttpMetrics setResponseHeaderDuration(long responseHeaderDuration) {
    this.responseHeaderDuration = Duration.ofMillis(responseHeaderDuration);
    return this;
  }

  HttpMetrics setResponseBodyDuration(long responseBodyDuration) {
    this.responseBodyDuration = Duration.ofMillis(responseBodyDuration);
    return this;
  }

  HttpMetrics setConnectionAttemptTime(long connectionAttemptTime) {
    this.connectionAttemptTime = Duration.ofMillis(connectionAttemptTime);
    return this;
  }

  HttpMetrics setSuccessfulConnectionTime(long successfulConnectionTime) {
    this.successfulConnectionTime = Duration.ofMillis(successfulConnectionTime);
    return this;
  }

  Duration getTotalCallDuration() {
    return totalCallDuration;
  }

  Duration getResponseHeaderDuration() {
    return responseHeaderDuration;
  }

  Duration getResponseBodyDuration() {
    return responseBodyDuration;
  }

  Duration getRequestBodyDuration() {
    return requestBodyDuration;
  }

  Duration getRequestHeaderDuration() {
    return requestHeaderDuration;
  }

  Duration getConnectionAttemptTime() {
    return this.connectionAttemptTime;
  }

  Duration getSuccessfulConnectionTime() {
    return this.successfulConnectionTime;
  }
}
