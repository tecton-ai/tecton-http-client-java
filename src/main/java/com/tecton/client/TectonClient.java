package com.tecton.client;

import com.tecton.client.exceptions.TectonClientException;
import com.tecton.client.exceptions.TectonErrorMessage;
import com.tecton.client.request.GetFeatureServiceMetadataRequest;
import com.tecton.client.request.GetFeaturesRequest;
import com.tecton.client.response.GetFeatureServiceMetadataResponse;
import com.tecton.client.response.GetFeaturesResponse;
import com.tecton.client.transport.TectonHttpClient;
import okhttp3.HttpUrl;

public class TectonClient {

  private TectonHttpClient tectonHttpClient;

  private final HttpUrl url;
  private final String apiKey;

  public TectonClient(String url, String apiKey) {
    if (url == null || url.isEmpty()) {
      throw new TectonClientException(TectonErrorMessage.INVALID_URL);
    }
    try {
      this.url = HttpUrl.parse(url);
    } catch (IllegalArgumentException e) {
      throw new TectonClientException(TectonErrorMessage.INVALID_URL);
    }
    if (apiKey == null || apiKey.isEmpty()) {
      throw new TectonClientException(TectonErrorMessage.EMPTY_KEY);
    }
    this.apiKey = apiKey;
    this.tectonHttpClient = new TectonHttpClient(new TectonClientOptions());
  }

  public TectonClient(String url, String apiKey, TectonClientOptions tectonClientOptions) {
    this(url, apiKey);
    this.tectonHttpClient = new TectonHttpClient(tectonClientOptions);
  }

  // TODO
  public GetFeaturesResponse getFeatures(GetFeaturesRequest getFeaturesRequest) {
    return null;
  }

  // TODO
  public GetFeatureServiceMetadataResponse getFeatureServiceMetadata(
      GetFeatureServiceMetadataRequest getFeatureServiceMetadataRequest) {
    return null;
  }
}
