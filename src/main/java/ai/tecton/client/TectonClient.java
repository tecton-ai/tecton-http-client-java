package ai.tecton.client;

import ai.tecton.client.exceptions.TectonClientException;
import ai.tecton.client.exceptions.TectonErrorMessage;
import ai.tecton.client.exceptions.TectonServiceException;
import ai.tecton.client.request.AbstractTectonRequest;
import ai.tecton.client.request.GetFeatureServiceMetadataRequest;
import ai.tecton.client.request.GetFeaturesBatchRequest;
import ai.tecton.client.request.GetFeaturesRequest;
import ai.tecton.client.response.GetFeatureServiceMetadataResponse;
import ai.tecton.client.response.GetFeaturesBatchResponse;
import ai.tecton.client.response.GetFeaturesResponse;
import ai.tecton.client.transport.HttpResponse;
import ai.tecton.client.transport.TectonHttpClient;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import okhttp3.OkHttpClient;

/**
 * A client for interacting with the Tecton FeatureService API. The client provides several methods
 * that make HTTP requests to the corresponding API endpoint and returns the response as a Java
 * object.
 *
 * <p>Note: Each method throws TectonServiceException when it receives an error response from the
 * API and a TectonClientException when an error or exception is encountered by the client. The
 * message included in the exception will provide more information about the error.
 */
public class TectonClient {

  private final TectonHttpClient tectonHttpClient;

  /**
   * Constructor for a simple Tecton Client
   *
   * @param url The Tecton Base Url
   * @param apiKey API Key for authenticating with the FeatureService API. See <a
   *     href="https://docs.tecton.ai/docs/reading-feature-data/reading-feature-data-for-inference/reading-online-features-for-inference-using-the-http-api#creating-an-api-key-to-authenticate-to-the-http-api">Authenticating
   *     with an API key</a> for more information
   */
  public TectonClient(String url, String apiKey) {
    this.tectonHttpClient =
        new TectonHttpClient(url, apiKey, new TectonClientOptions.Builder().build());
  }

  /**
   * Constructor for a Tecton Client with custom configurations
   *
   * @param url The Tecton Base Url
   * @param apiKey API Key for authenticating with the FeatureService API. See <a
   *     href="https://docs.tecton.ai/docs/reading-feature-data/reading-feature-data-for-inference/reading-online-features-for-inference-using-the-http-api#creating-an-api-key-to-authenticate-to-the-http-api">Authenticating
   *     with an API key</a> for more information
   * @param tectonClientOptions A {@link TectonClientOptions} object with custom configurations
   */
  public TectonClient(String url, String apiKey, TectonClientOptions tectonClientOptions) {
    this.tectonHttpClient = new TectonHttpClient(url, apiKey, tectonClientOptions);
  }

  /**
   * Constructor for a Tecton Client with a custom OkHttpClient
   *
   * @param url The Tecton Base Url
   * @param apiKey API Key for authenticating with the FeatureService API. See <a
   *     href="https://docs.tecton.ai/docs/reading-feature-data/reading-feature-data-for-inference/reading-online-features-for-inference-using-the-http-api#creating-an-api-key-to-authenticate-to-the-http-api">Authenticating
   *     with an API key</a> for more information
   * @param httpClient An OkHttpClient for making requests and receiving responses from the Feature
   *     Service API. Please refer to <a
   *     href="https://square.github.io/okhttp/4.x/okhttp/okhttp3/-ok-http-client/">OkHttp
   *     Documentation</a> for recommendations on creating and maintaining an OkHttp Client in your
   *     application. Tecton recommends configuring the <a
   *     href="https://square.github.io/okhttp/4.x/okhttp/okhttp3/-connection-pool">ConnectionPool</a>
   *     in the OkHttpClient for efficiently managing HTTP connections. If you intend to use the
   *     {@link GetFeaturesBatchRequest} to send parallel requests to Tecton, please also configure
   *     the <a
   *     href="https://square.github.io/okhttp/4.x/okhttp/okhttp3/-dispatcher/max-requests-per-host">maxRequestsPerHost</a>
   *     in the client's Dispatcher.
   */
  public TectonClient(String url, String apiKey, OkHttpClient httpClient) {
    this.tectonHttpClient = new TectonHttpClient(url, apiKey, httpClient);
  }

  /**
   * Makes a request to the /get-features endpoint and returns the response in the form of a {@link
   * GetFeaturesResponse} object
   *
   * @param getFeaturesRequest A {@link GetFeaturesRequest} object with the request parameters
   * @return {@link GetFeaturesResponse} object representing the response from the HTTP API
   * @throws TectonClientException when the client is already closed or encounters an error while
   *     building the request or parsing the response
   * @throws TectonServiceException when the client receives an error response from the HTTP API
   */
  public GetFeaturesResponse getFeatures(GetFeaturesRequest getFeaturesRequest)
      throws TectonClientException, TectonServiceException {
    HttpResponse httpResponse = getHttpResponse(getFeaturesRequest);
    return new GetFeaturesResponse(
        httpResponse.getResponseBody().get(), httpResponse.getRequestDuration());
  }

  /**
   * Makes a request to the /metadata endpoint and returns the response in the form of a {@link
   * GetFeatureServiceMetadataResponse} object
   *
   * @param getFeatureServiceMetadataRequest A {@link GetFeatureServiceMetadataRequest} object with
   *     the request parameters
   * @return {@link GetFeatureServiceMetadataResponse} object representing the response from the
   *     HTTP API
   * @throws TectonClientException when the client is already closed or encounters an error while
   *     building the request or parsing the response
   * @throws TectonServiceException when the client receives an error response from the HTTP API
   */
  public GetFeatureServiceMetadataResponse getFeatureServiceMetadata(
      GetFeatureServiceMetadataRequest getFeatureServiceMetadataRequest)
      throws TectonClientException, TectonServiceException {
    HttpResponse httpResponse = getHttpResponse(getFeatureServiceMetadataRequest);
    return new GetFeatureServiceMetadataResponse(
        httpResponse.getResponseBody().get(), httpResponse.getRequestDuration());
  }

  /**
   * Makes a batch request to retrieve a list of feature vector and metadata for a given workspace
   * and feature service
   *
   * @param batchRequest The {@link GetFeaturesRequest} object with the request parameters
   * @return {@link GetFeaturesBatchResponse} object with the list of feature vector and metadata (if
   *     requested)
   * @throws TectonClientException when the client is already closed or the client encounters an
   *     error while building the request or parsing the response
   * @throws TectonServiceException when the client receives an error response from the HTTP API
   */
  public GetFeaturesBatchResponse getFeaturesBatch(GetFeaturesBatchRequest batchRequest)
      throws TectonClientException, TectonServiceException {
    // Serialize batch request into list of JSON request
    List<String> requestList =
        batchRequest.getRequestList().stream()
            .map(AbstractTectonRequest::requestToJson)
            .collect(Collectors.toList());

    // Perform parallel batch call
    long start = System.currentTimeMillis();
    List<HttpResponse> httpResponseList =
        tectonHttpClient.performParallelRequests(
            batchRequest.getEndpoint(),
            batchRequest.getMethod(),
            requestList,
            batchRequest.getTimeout());
    long stop = System.currentTimeMillis();
    Duration totalTime = Duration.ofMillis(stop - start);

    // Deserialize list of JSON responses into a GetFeaturesBatchResponse
    return new GetFeaturesBatchResponse(
        httpResponseList, totalTime, batchRequest.getMicroBatchSize());
  }

  /**
   * Releases all resources (connection pool, threads) held by the Tecton Client. Once the client is
   * closed, all future calls with the client will fail.
   */
  public void close() {
    if (!this.isClosed()) {
      this.tectonHttpClient.close();
    }
  }

  /**
   * Returns true if the Tecton Client has been closed, false otherwise
   *
   * @return boolean
   */
  public boolean isClosed() {
    return tectonHttpClient.isClosed();
  }

  private HttpResponse getHttpResponse(AbstractTectonRequest tectonRequest) {
    // Perform request and get HttpResponse
    HttpResponse httpResponse =
        tectonHttpClient.performRequest(
            tectonRequest.getEndpoint(), tectonRequest.getMethod(), tectonRequest.requestToJson());

    if (!httpResponse.isSuccessful()) {
      throw new TectonServiceException(
          String.format(
              TectonErrorMessage.ERROR_RESPONSE,
              httpResponse.getResponseCode(),
              httpResponse.getMessage()));
    }
    if (!httpResponse.getResponseBody().isPresent()) {
      throw new TectonClientException(TectonErrorMessage.EMPTY_RESPONSE);
    }
    return httpResponse;
  }

  /**
   * A Builder class for creating an instance of {@link TectonClient} object with specific
   * configurations
   */
  public static class Builder {
    private String url;
    private String apiKey;
    private TectonClientOptions tectonClientOptions;
    private OkHttpClient httpClient;

    /**
     * Setter for url
     *
     * @param url The Tecton Base Url
     * @return this Builder
     */
    public Builder url(String url) {
      this.url = url;
      return this;
    }

    /**
     * Setter for apiKey
     *
     * @param apiKey API Key for authenticating with the FeatureService API. See <a
     *     href="https://docs.tecton.ai/docs/reading-feature-data/reading-feature-data-for-inference/reading-online-features-for-inference-using-the-http-api#creating-an-api-key-to-authenticate-to-the-http-api">Authenticating
     *     with an API key</a> for more information
     * @return this Builder
     */
    public Builder apiKey(String apiKey) {
      this.apiKey = apiKey;
      return this;
    }

    /**
     * Setter for tectonClientOptions
     *
     * @param tectonClientOptions A {@link TectonClientOptions} object with custom configurations
     * @return this Builder
     */
    public Builder tectonClientOptions(TectonClientOptions tectonClientOptions) {
      this.tectonClientOptions = tectonClientOptions;
      return this;
    }

    /**
     * Setter for httpClient
     *
     * @param httpClient An OkHttpClient for making requests and receiving responses from the
     *     Feature Service API. Please refer to <a
     *     href="https://square.github.io/okhttp/4.x/okhttp/okhttp3/-ok-http-client/">OkHttp
     *     Documentation</a> for recommendations on creating and maintaining an OkHttp Client in
     *     your application. Tecton recommends configuring the <a
     *     href="https://square.github.io/okhttp/4.x/okhttp/okhttp3/-connection-pool">ConnectionPool</a>
     *     in the OkHttpClient for efficiently managing HTTP connections. If you intend to use the
     *     {@link GetFeaturesBatchRequest} to send parallel requests to Tecton, please also
     *     configure the <a
     *     href="https://square.github.io/okhttp/4.x/okhttp/okhttp3/-dispatcher/max-requests-per-host">maxRequestsPerHost</a>
     *     in the client's Dispatcher.
     * @return this Builder
     */
    public Builder httpClient(OkHttpClient httpClient) {
      this.httpClient = httpClient;
      return this;
    }

    /**
     * Build a {@link TectonClient} object from the Builder
     *
     * @return {@link TectonClient}
     */
    public TectonClient build() {
      if (this.httpClient != null) {
        return new TectonClient(url, apiKey, httpClient);
      } else if (this.tectonClientOptions != null) {
        return new TectonClient(url, apiKey, tectonClientOptions);
      }
      return new TectonClient(url, apiKey);
    }
  }
}
