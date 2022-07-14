package com.tecton.client.exceptions;

/**
 * Class that declares all the different error messages included in the {@link
 * com.tecton.client.exceptions.TectonClientException}
 */
public class TectonErrorMessage {
  public static final String INVALID_KEY = "API Key cannot be empty";
  public static final String INVALID_URL = "Cannot connect to Tecton because the URL is invalid";
  public static final String INVALID_KEY_VALUE = "Key/Value cannot be null or empty";

  public static final String ERROR_RESPONSE =
      "Received Error Response from Tecton wih code %s and error message: %s";

  public static final String INVALID_WORKSPACENAME = "Workspace Name cannot be null or empty";
  public static final String INVALID_FEATURESERVICENAME =
      "FeatureService Name cannot be null or empty";
  public static final String EMPTY_REQUEST_MAPS =
      "Both Join Key map and Request Context Map cannot be empty";

  public static final String INVALID_GET_FEATURE_REQUEST =
      "The parameters passed to the GetFeatureRequest are invalid. %s";
  public static final String INVALID_GET_SERVICE_METADATA_REQUEST =
      "The parameters passed to the GetFeatureServiceMetadataRequest are invalid. %s";
  public static final String INVALID_RESPONSE_FORMAT = "Unable to parse JSON response from Tecton";
  public static final String EMPTY_RESPONSE = "Received empty response body from Tecton";
  public static final String EMPTY_FEATURE_VECTOR = "Received empty feature vector from Tecton";

  public static final String MISSING_EXPECTED_METADATA =
      "Required metadata %s is missing in the response";
  public static final String UNKNOWN_DATA_TYPE = "Unknown Data Type %s in response";
  public static final String MISMATCHED_TYPE = "Invalid method used to access value of type %s";
  public static final String UNSUPPORTED_LIST_DATA_TYPE =
      "Unsupported data type detected for array feature values";
}
