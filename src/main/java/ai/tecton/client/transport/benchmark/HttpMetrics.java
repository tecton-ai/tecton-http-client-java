package ai.tecton.client.transport.benchmark;

public class HttpMetrics {
  private final long totalCallDuration;
  private final long requestHeaderDuration;
  private final long requestBodyDuration;
  private final long responseHeaderDuration;
  private final long responseBodyDuration;
  private final long responseLatency;

  public HttpMetrics(OkhttpCallLog okHttpCall) {
    this.totalCallDuration = okHttpCall.callEnd - okHttpCall.callStart;
    this.requestHeaderDuration = okHttpCall.requestHeadersEnd - okHttpCall.requestHeadersStart;
    this.requestBodyDuration = okHttpCall.requestBodyEnd - okHttpCall.requestBodyStart;
    this.responseHeaderDuration = okHttpCall.responseHeadersEnd - okHttpCall.responseHeadersStart;
    this.responseBodyDuration = okHttpCall.responseBodyEnd - okHttpCall.responseBodyStart;
    this.responseLatency = okHttpCall.responseHeadersStart - okHttpCall.requestBodyEnd;
  }

  long getTotalCallDuration() {
    return totalCallDuration;
  }

  long getResponseHeaderDuration() {
    return responseHeaderDuration;
  }

  long getResponseBodyDuration() {
    return responseBodyDuration;
  }

  long getRequestBodyDuration() {
    return requestBodyDuration;
  }

  long getRequestHeaderDuration() {
    return requestHeaderDuration;
  }

  long getResponseLatency() {
    return responseLatency;
  }
}
