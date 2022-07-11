package com.tecton.client;

import java.time.Duration;

public class TectonClientOptions {

  Duration readTimeout;
  Duration connectTimeout;
  int maxIdleConnections;
  Duration keepAliveDuration;

  public TectonClientOptions() {
    readTimeout = Duration.ofSeconds(5);
    connectTimeout = Duration.ofSeconds(5);
    maxIdleConnections = 5;
    keepAliveDuration = Duration.ofMinutes(5);
  }

  public Duration getConnectTimeout() {
    return connectTimeout;
  }

  public Duration getKeepAliveDuration() {
    return keepAliveDuration;
  }

  public Duration getReadTimeout() {
    return readTimeout;
  }

  public int getMaxIdleConnections() {
    return maxIdleConnections;
  }

  public TectonClientOptions setReadTimeout(Duration readTimeout) {
    this.readTimeout = readTimeout;
    return this;
  }

  public TectonClientOptions setConnectTimeout(Duration connectTimeout) {
    this.connectTimeout = connectTimeout;
    return this;
  }

  public TectonClientOptions setMaxIdleConnections(int maxIdleConnections) {
    this.maxIdleConnections = maxIdleConnections;
    return this;
  }

  public TectonClientOptions setKeepAliveDuration(Duration keepAliveDuration) {
    this.keepAliveDuration = keepAliveDuration;
    return this;
  }
}
