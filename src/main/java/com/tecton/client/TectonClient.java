package com.tecton.client;

import com.tecton.client.exceptions.TectonClientException;
import com.tecton.client.exceptions.TectonErrorMessage;
import com.tecton.client.exceptions.TectonServiceException;
import com.tecton.client.request.GetFeatureServiceMetadataRequest;
import com.tecton.client.request.GetFeaturesRequest;
import com.tecton.client.response.GetFeatureServiceMetadataResponse;
import com.tecton.client.response.GetFeaturesResponse;
import com.tecton.client.transport.HttpResponse;
import com.tecton.client.transport.TectonHttpClient;

public class TectonClient {

  private final TectonHttpClient tectonHttpClient;

  public TectonClient(String url, String apiKey) {
    this.tectonHttpClient = new TectonHttpClient(url, apiKey, new TectonClientOptions());
  }

  public TectonClient(String url, String apiKey, TectonClientOptions tectonClientOptions) {
    this.tectonHttpClient = new TectonHttpClient(url, apiKey, tectonClientOptions);
  }

  public GetFeaturesResponse getFeatures(GetFeaturesRequest getFeaturesRequest) {
    HttpResponse httpResponse =
        tectonHttpClient.performRequest(
            getFeaturesRequest.getEndpoint(),
            getFeaturesRequest.getMethod(),
            getFeaturesRequest.requestToJson());
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
}
