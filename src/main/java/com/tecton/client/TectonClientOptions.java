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
}
