package ai.tecton.client;

import java.time.Duration;

/**
 * A class that provides custom configuration options for the underlying Http Client. The Client
 * currently supports customizing the following configurations:
 *
 * <ul>
 *   <li><a
 *       href="https://square.github.io/okhttp/4.x/okhttp/okhttp3/-ok-http-client/-builder/read-timeout/">Read
 *       Timeout</a> - Default = 5s
 *   <li><a
 *       href="https://square.github.io/okhttp/4.x/okhttp/okhttp3/-ok-http-client/-builder/connect-timeout/">Connect
 *       Timeout</a> - Default = 5s
 *   <li><a href="https://square.github.io/okhttp/3.x/okhttp/okhttp3/ConnectionPool.html/">Max Idle
 *       Connections</a> - Default = 5
 *   <li><a href="https://square.github.io/okhttp/3.x/okhttp/okhttp3/ConnectionPool.html/">Keep
 *       Alive Duration</a> - Default = 5mins
 *   <li><a
 *       href="https://square.github.io/okhttp/4.x/okhttp/okhttp3/-dispatcher/max-requests-per-host//">Max
 *       Parallel Requests</a> - Default = 5
 * </ul>
 */
public class TectonClientOptions {

  private static final Duration DEFAULT_READ_TIMEOUT = Duration.ofSeconds(2);
  private static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(2);
  private static final int DEFAULT_MAX_IDLE_CONNECTIONS = 5;
  private static final Duration DEFAULT_KEEPALIVE_DURATION = Duration.ofMinutes(5);
  private static final int DEFAULT_MAX_PARALLEL_REQUESTS = 5;

  private final Duration readTimeout;
  private final Duration connectTimeout;
  private final int maxIdleConnections;
  private final Duration keepAliveDuration;
  private final int maxParallelRequests;

  /**
   * Constructor that instantiates a TectonClientOptions with default configurations
   *
   * <p>Read Timeout = 5 seconds, Connect Timeout = 5 seconds, Max Idle Connections = 5, Keep Alive
   * Duration = 5 minutes, Max Parallel Requests = 5
   */
  public TectonClientOptions() {
    this.readTimeout = DEFAULT_READ_TIMEOUT;
    this.connectTimeout = DEFAULT_CONNECT_TIMEOUT;
    this.maxIdleConnections = DEFAULT_MAX_IDLE_CONNECTIONS;
    this.keepAliveDuration = DEFAULT_KEEPALIVE_DURATION;
    this.maxParallelRequests = DEFAULT_MAX_PARALLEL_REQUESTS;
  }
  /** Constructor that configures the TectonClientOptions with specified configurations */
  public TectonClientOptions(
      Duration readTimeout,
      Duration connectTimeout,
      int maxIdleConnections,
      Duration keepAliveDuration,
      int maxParallelRequests) {
    this.readTimeout = readTimeout;
    this.connectTimeout = connectTimeout;
    this.maxIdleConnections = maxIdleConnections;
    this.keepAliveDuration = keepAliveDuration;
    this.maxParallelRequests = maxParallelRequests;
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
   * Returns the maxParallelRequests configuration for the client
   *
   * @return maxParallelRequests
   */
  public int getMaxParallelRequests() {
    return maxParallelRequests;
  }

  /**
   * A Builder class for creating an instance of {@link TectonClientOptions} object with specific
   * configurations
   */
  public static class Builder {
    private Duration readTimeout = DEFAULT_READ_TIMEOUT;
    private Duration connectTimeout = DEFAULT_CONNECT_TIMEOUT;
    private int maxIdleConnections = DEFAULT_MAX_IDLE_CONNECTIONS;
    private Duration keepAliveDuration = DEFAULT_KEEPALIVE_DURATION;
    private int maxParallelRequests = DEFAULT_MAX_PARALLEL_REQUESTS;

    /**
     * Setter for the readTimeout value for new connections. A value of 0 means no timeout,
     * otherwise values must be between 1 and Integer.MAX_VALUE when converted to milliseconds
     *
     * @param readTimeout readTimeout as a {@link java.time.Duration}
     * @return this Builder
     */
    public Builder readTimeout(Duration readTimeout) {
      this.readTimeout = readTimeout;
      return this;
    }

    /**
     * Setter for the connectTimeout value for new connections. A value of 0 means no timeout,
     * otherwise values must be between 1 and Integer.MAX_VALUE when converted to milliseconds
     *
     * @param connectTimeout connectTimeout as a {@link java.time.Duration}
     * @return this Builder
     */
    public Builder connectTimeout(Duration connectTimeout) {
      this.connectTimeout = connectTimeout;
      return this;
    }

    /**
     * Setter for the maximum number of idle connections to keep in the pool. If not set, the client
     * will use a default value of 5
     *
     * @param maxIdleConnections int value, must be between 1 and Integer.MAX_VALUE
     * @return this builder
     */
    public Builder maxIdleConnections(int maxIdleConnections) {
      this.maxIdleConnections = maxIdleConnections;
      return this;
    }

    /**
     * Setter for the time to keep an idle connection alive in the pool before closing it
     *
     * @param keepAliveDuration as a {@link java.time.Duration}
     * @return this Builder
     */
    public Builder keepAliveDuration(Duration keepAliveDuration) {
      this.keepAliveDuration = keepAliveDuration;
      return this;
    }

    /**
     * Setter for the maximum number of requests to execute concurrently. Above this requests queue
     * in memory, waiting for the running calls to complete. Default value is 5
     *
     * @param maxParallelRequests int value, must be between 1 and Integer.MAX_VALUE
     * @return this Builder
     */
    public Builder maxParallelRequests(int maxParallelRequests) {
      this.maxParallelRequests = maxParallelRequests;
      return this;
    }

    /**
     * Build a {@link TectonClientOptions} object from the Builder
     *
     * @return {@link TectonClientOptions}
     */
    public TectonClientOptions build() {
      return new TectonClientOptions(
          this.readTimeout,
          this.connectTimeout,
          this.maxIdleConnections,
          this.keepAliveDuration,
          this.maxParallelRequests);
    }
  }
}
