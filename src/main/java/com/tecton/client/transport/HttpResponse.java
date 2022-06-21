package com.tecton.client.transport;
import okhttp3.Headers;
import okhttp3.Response;

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;

public class HttpResponse {
    private final boolean isSuccessful;
    private final int responseCode;
    private final String message;
    private final Optional<String> body;
    private final Headers headers;
    private final Duration requestDuration;

    public HttpResponse(Response response) throws IOException {
        this.responseCode = response.code();
        this.headers = response.headers();
        this.requestDuration = Duration.ofMillis(response.receivedResponseAtMillis()-response.sentRequestAtMillis());
        this.isSuccessful = response.isSuccessful();
        this.message = response.message();

        if(response.isSuccessful()) {
            this.body = Optional.of(response.body().string());
        } else {
            body = Optional.empty();
        }
    }
}
