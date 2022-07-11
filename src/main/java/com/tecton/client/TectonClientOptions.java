package com.tecton.client;

import java.time.Duration;

/**
 * A class that provides custom configuration options for the underlying Http Client. The Client
 * currently supports customizing the following configurations:
 *
 * <ul>
 *   <li><a
 *       href="https://square.github.io/okhttp/4.x/okhttp/okhttp3/-ok-http-client/-builder/read-timeout/">Read
 *       Timeout</a>
 *   <li><a
 *       href="https://square.github.io/okhttp/4.x/okhttp/okhttp3/-ok-http-client/-builder/connect-timeout/">Connect
 *       Timeout</a>
 *   <li><a href="https://square.github.io/okhttp/3.x/okhttp/okhttp3/ConnectionPool.html/">Max Idle
 *       Connections</a>
 *   <li><a href="https://square.github.io/okhttp/3.x/okhttp/okhttp3/ConnectionPool.html/">Keep
 *       Alive Duration</a>
 * </ul>
 */
public class TectonClientOptions {

  private Duration readTimeout;
  private Duration connectTimeout;
  private int maxIdleConnections;
  private Duration keepAliveDuration;

  /**
   * Constructor that configures the TectonClient with default configurations
   *
   * <p>Read Timeout = 5 seconds, Connect Timeout = 5 seconds, Max Idle Connections = 5, Keep Alive
   * Duration = 5 minutes
   */
  public TectonClientOptions() {
    readTimeout = Duration.ofSeconds(5);
    connectTimeout = Duration.ofSeconds(5);
    maxIdleConnections = 5;
    keepAliveDuration = Duration.ofMinutes(5);
  }

  /**
   * Returns the connectTimeout configuration for the client
   *
   * @return connectTimeout represented as a {@link java.time.Duration}
   */
  public Duration getConnectTimeout() {
    return connectTimeout;
  }

  /**
   * Returns the keepAliveDuration configuration for the client's ConnectionPool
   *
   * @return keepAliveDuration represented as a {@link java.time.Duration}
   */
  public Duration getKeepAliveDuration() {
    return keepAliveDuration;
  }

  /**
   * Returns the readTimeout configuration for the client
   *
   * @return readTimeout represented as a {@link java.time.Duration}
   */
  public Duration getReadTimeout() {
    return readTimeout;
  }

  /**
   * Returns the maxIdleConnections configuration for the client's ConnectionPool
   *
   * @return maxIdleConnections
   */
  public int getMaxIdleConnections() {
    return maxIdleConnections;
  }

  /**
   * Sets the readTimeout value for new connections. A value of 0 means no timeout, otherwise values
   * must be between 1 and Integer.MAX_VALUE when converted to milliseconds
   *
   * @param readTimeout readTimeout as a {@link java.time.Duration}
   * @return The {@link com.tecton.client.TectonClientOptions} object after configuring the
   *     readTimeout
   */
  public TectonClientOptions setReadTimeout(Duration readTimeout) {
    this.readTimeout = readTimeout;
    return this;
  }

  /**
   * Sets the connectTimeout value for new connections. A value of 0 means no timeout, otherwise
   * values must be between 1 and Integer.MAX_VALUE when converted to milliseconds
   *
   * @param connectTimeout connectTimeout as a {@link java.time.Duration}
   * @return The {@link com.tecton.client.TectonClientOptions} object after configuring the
   *     connectTimeout
   */
  public TectonClientOptions setConnectTimeout(Duration connectTimeout) {
    this.connectTimeout = connectTimeout;
    return this;
  }

  /**
   * Sets the maximum number of idle connections to keep in the pool.
   *
   * @param maxIdleConnections
   * @return The {@link com.tecton.client.TectonClientOptions} object after configuring the
   *     maxIdleConnections
   */
  public TectonClientOptions setMaxIdleConnections(int maxIdleConnections) {
    this.maxIdleConnections = maxIdleConnections;
    return this;
  }

  /**
   * Sets the time to keep an idle connection alive in the pool before closing it
   *
   * @param keepAliveDuration as a {@link java.time.Duration}
   * @return The {@link com.tecton.client.TectonClientOptions} object after configuring the *
   *     keepAliveDuration
   */
  public TectonClientOptions setKeepAliveDuration(Duration keepAliveDuration) {
    this.keepAliveDuration = keepAliveDuration;
    return this;
  }
}
