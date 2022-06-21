package com.tecton.client.exceptions;

public class TectonErrorMessage {
    public static final String INVALID_KEY = "API Key cannot be empty";
    public static final String INVALID_URL = "Cannot connect to Tecton because the URL is invalid";
    public static final String INVALID_KEY_VALUE = "Key/Value cannot be null or empty";

    public static final String ERROR_RESPONSE = "Request to Tecton failed with HTTP Status %s. Tecton Error Message: %s";
    public static final String CONNECTION_TIMEOUT = "The request could not be completed because the connection to %s timed out";

    public static final String INVALID_WORKSPACENAME = "Workspace Name cannot be null or empty";
    public static final String INVALID_FEATURESERVICENAME = "FeatureService Name cannot be null or empty";
    public static final String EMPTY_REQUEST_MAPS = "Both Join Key map and Request Context Map cannot be empty";

    public static final String INVALID_GET_FEATURE_REQUEST = "The parameters passed to the GetFeatureRequest are invalid";
}
