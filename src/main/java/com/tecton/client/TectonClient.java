package com.tecton.client;

import com.tecton.client.exceptions.TectonClientException;
import com.tecton.client.exceptions.TectonErrorMessage;
import com.tecton.client.exceptions.TectonServiceException;
import com.tecton.client.request.GetFeatureServiceMetadataRequest;
import com.tecton.client.request.GetFeaturesRequest;
import com.tecton.client.response.GetFeatureServiceMetadataResponse;
import com.tecton.client.response.GetFeaturesResponse;
import com.tecton.client.transport.HttpRequest;
import com.tecton.client.transport.HttpResponse;
import com.tecton.client.transport.TectonHttpClient;
import okhttp3.HttpUrl;
import org.apache.commons.lang3.Validate;

public class TectonClient {

  private final TectonHttpClient tectonHttpClient;

  private HttpUrl url;
  private final String apiKey;

  public TectonClient(String url, String apiKey) {
    validateClientParameters(url, apiKey);
    this.apiKey = apiKey;
    this.tectonHttpClient = new TectonHttpClient(new TectonClientOptions());
  }

  public TectonClient(String url, String apiKey, TectonClientOptions tectonClientOptions) {
    validateClientParameters(url, apiKey);
    this.apiKey = apiKey;
    this.tectonHttpClient = new TectonHttpClient(tectonClientOptions);
  }

  public GetFeaturesResponse getFeatures(GetFeaturesRequest getFeaturesRequest) {
    String requestBody = getFeaturesRequest.requestToJson();
    HttpRequest httpRequest =
        new HttpRequest(
            url.url().toString(),
            getFeaturesRequest.getEndpoint(),
            getFeaturesRequest.getMethod(),
            apiKey,
            requestBody);
    HttpResponse httpResponse = tectonHttpClient.performRequest(httpRequest);
    if (httpResponse.isSuccessful()) {
      if (!httpResponse.getResponseBody().isPresent()) {
        throw new TectonClientException(TectonErrorMessage.EMPTY_RESPONSE);
      }
      return new GetFeaturesResponse(
          httpResponse.getResponseBody().get(), httpResponse.getRequestDuration());
    } else {
      throw new TectonServiceException(
          String.format(
              TectonErrorMessage.ERROR_RESPONSE,
              httpResponse.getResponseCode(),
              httpResponse.getMessage()));
    }
  }

  // TODO
  public GetFeatureServiceMetadataResponse getFeatureServiceMetadata(
      GetFeatureServiceMetadataRequest getFeatureServiceMetadataRequest) {
    return null;
  }

  private void validateClientParameters(String url, String apiKey) {
    try {
      Validate.notEmpty(apiKey);
    } catch (Exception e) {
      throw new TectonClientException(TectonErrorMessage.INVALID_KEY);
    }

    try {
      Validate.notEmpty(url);
      this.url = HttpUrl.parse(url);
    } catch (Exception e) {
      throw new TectonClientException(TectonErrorMessage.INVALID_URL);
    }
  }
}
