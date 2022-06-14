package com.tecton.client.exceptions;

public class TectonErrorMessage {
    public static final String EMPTY_KEY = "Api Key is null or empty";
    public static final String INVALID_URL = "Cannot connect to Tecton because the URL is invalid";
    public static final String ERROR_RESPONSE = "Request to Tecton failed with HTTP Status %s. Tecton Error Message: %s";
    public static final String CONNECTION_TIMEOUT = "The request could not be completed because the connection to %s timed out";
}
