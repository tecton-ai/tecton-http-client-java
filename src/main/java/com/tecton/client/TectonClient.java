package com.tecton.client;

import com.tecton.client.exceptions.TectonClientException;
import com.tecton.client.exceptions.TectonErrorMessage;
import com.tecton.client.request.GetFeatureServiceMetadataRequest;
import com.tecton.client.request.GetFeaturesRequest;
import com.tecton.client.response.GetFeatureServiceMetadataResponse;
import com.tecton.client.response.GetFeaturesResponse;
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
   return null;
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
