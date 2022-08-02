package ai.tecton.client;

import ai.tecton.client.exceptions.TectonClientException;
import ai.tecton.client.exceptions.TectonErrorMessage;
import ai.tecton.client.exceptions.TectonServiceException;
import ai.tecton.client.request.AbstractTectonRequest;
import ai.tecton.client.request.GetFeatureServiceMetadataRequest;
import ai.tecton.client.request.GetFeaturesBatchRequest;
import ai.tecton.client.request.GetFeaturesRequest;
import ai.tecton.client.response.GetFeatureServiceMetadataResponse;
import ai.tecton.client.response.GetFeaturesResponse;
import ai.tecton.client.response.GetFeaturesResponseBatch;
import ai.tecton.client.transport.HttpResponse;
import ai.tecton.client.transport.TectonHttpClient;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import java.io.IOException;

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
  Moshi moshi = new Moshi.Builder().build();
  private final JsonAdapter<ErrorResponseJson> jsonAdapter = moshi.adapter(ErrorResponseJson.class);

  /**
   * Constructor for a simple Tecton Client
   *
   * @param url The Tecton Base Url
   * @param apiKey API Key for authenticating with the FeatureService API. See <a
   *     href="https://docs.tecton.ai/latest/examples/fetch-real-time-features.html#authenticating-with-an-api-key/">Authenticating
   *     with an API key</a> for more information
   */
  public TectonClient(String url, String apiKey) {
    this.tectonHttpClient = new TectonHttpClient(url, apiKey, new TectonClientOptions());
  }

  /**
   * Constructor for a Tecton Client with custom configurations
   *
   * @param url The Tecton Base Url
   * @param apiKey API Key for authenticating with the FeatureService API. See <a
   *     href="https://docs.tecton.ai/latest/examples/fetch-real-time-features.html#authenticating-with-an-api-key/">Authenticating
   *     with an API key</a> for more information
   * @param tectonClientOptions A {@link TectonClientOptions} object with custom comfigurations
   */
  public TectonClient(String url, String apiKey, TectonClientOptions tectonClientOptions) {
    this.tectonHttpClient = new TectonHttpClient(url, apiKey, tectonClientOptions);
  }

  /**
   * Makes a request to the /get-features endpoint and returns the response in the form of a {@link
   * GetFeaturesResponse} object
   *
   * @param getFeaturesRequest A {@link GetFeaturesRequest} object with the request parameters
   * @return {@link GetFeaturesResponse} object representing the response from the HTTP API
   * @throws TectonClientException when the client encounters an error while building the request or
   *     parsing the response
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
   * @return {@link GetFeatureServiceMetadataResponse} object representing the response * from the
   *     HTTP API
   * @throws TectonClientException when the client encounters an error while building the request or
   *     * parsing the response
   * @throws TectonServiceException when the client receives an error response from the HTTP API
   */
  public GetFeatureServiceMetadataResponse getFeatureServiceMetadata(
      GetFeatureServiceMetadataRequest getFeatureServiceMetadataRequest)
      throws TectonClientException, TectonServiceException {
    HttpResponse httpResponse = getHttpResponse(getFeatureServiceMetadataRequest);
    return new GetFeatureServiceMetadataResponse(
        httpResponse.getResponseBody().get(), httpResponse.getRequestDuration());
  }

  public GetFeaturesResponseBatch getFeaturesResponseBatch(
      GetFeaturesBatchRequest getFeaturesBatchRequest) {
    return null;
  }

  private HttpResponse getHttpResponse(AbstractTectonRequest tectonRequest) {
    // Perform request and get HttpResponse
    HttpResponse httpResponse =
        tectonHttpClient.performRequest(
            tectonRequest.getEndpoint(), tectonRequest.getMethod(), tectonRequest.requestToJson());

    if (httpResponse.isSuccessful()) {
      if (!httpResponse.getResponseBody().isPresent()) {
        throw new TectonClientException(TectonErrorMessage.EMPTY_RESPONSE);
      }
      return httpResponse;
    } else {
      // Parse error response and throw TectonServiceException
      String errorMessage = httpResponse.getMessage();
      if (httpResponse.getResponseBody().isPresent()) {
        try {
          ErrorResponseJson errorResponseJson =
              jsonAdapter.fromJson(httpResponse.getResponseBody().get());
          errorMessage = errorResponseJson.message;
        } catch (IOException e) {
          throw new TectonClientException(TectonErrorMessage.INVALID_RESPONSE_FORMAT);
        } catch (Exception ignored) {
        }
      }
      throw new TectonServiceException(
          String.format(
              TectonErrorMessage.ERROR_RESPONSE, httpResponse.getResponseCode(), errorMessage));
    }
  }

  static class ErrorResponseJson {
    String error;
    int code;
    String message;
  }
}
