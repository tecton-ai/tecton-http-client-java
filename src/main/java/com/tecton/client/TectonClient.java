package com.tecton.client;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.tecton.client.exceptions.TectonClientException;
import com.tecton.client.exceptions.TectonErrorMessage;
import com.tecton.client.exceptions.TectonServiceException;
import com.tecton.client.request.AbstractTectonRequest;
import com.tecton.client.request.GetFeatureServiceMetadataRequest;
import com.tecton.client.request.GetFeaturesRequest;
import com.tecton.client.response.GetFeatureServiceMetadataResponse;
import com.tecton.client.response.GetFeaturesResponse;
import com.tecton.client.transport.HttpResponse;
import com.tecton.client.transport.TectonHttpClient;

public class TectonClient {

  private final TectonHttpClient tectonHttpClient;
  Moshi moshi = new Moshi.Builder().build();
  private final JsonAdapter<ErrorResponseJson> jsonAdapter = moshi.adapter(ErrorResponseJson.class);

  public TectonClient(String url, String apiKey) {
    this.tectonHttpClient = new TectonHttpClient(url, apiKey, new TectonClientOptions());
  }

  public TectonClient(String url, String apiKey, TectonClientOptions tectonClientOptions) {
    this.tectonHttpClient = new TectonHttpClient(url, apiKey, tectonClientOptions);
  }

  public GetFeaturesResponse getFeatures(GetFeaturesRequest getFeaturesRequest) {
    HttpResponse httpResponse = getHttpResponse(getFeaturesRequest);
    return new GetFeaturesResponse(
        httpResponse.getResponseBody().get(), httpResponse.getRequestDuration());
  }

  public GetFeatureServiceMetadataResponse getFeatureServiceMetadata(
      GetFeatureServiceMetadataRequest getFeatureServiceMetadataRequest) {
    HttpResponse httpResponse = getHttpResponse(getFeatureServiceMetadataRequest);
    return new GetFeatureServiceMetadataResponse(
        httpResponse.getResponseBody().get(), httpResponse.getRequestDuration());
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
