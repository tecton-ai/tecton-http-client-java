package ai.tecton.client.transport.benchmark;

public class CallMetrics {
  private final long totalCallDuration;
  private final long requestHeaderDuration;
  private final long requestBodyDuration;
  private final long responseHeaderDuration;
  private final long responseBodyDuration;
  private final long responseLatency;
  private long totalDuration;
  private long clientLatency;
  private boolean isSuccessful;

  public CallMetrics(OkhttpCallLog okHttpCall) {
    this.totalCallDuration = okHttpCall.callEnd - okHttpCall.callStart;
    this.requestHeaderDuration = okHttpCall.requestHeadersEnd - okHttpCall.requestHeadersStart;
    this.requestBodyDuration = okHttpCall.requestBodyEnd - okHttpCall.requestBodyStart;
    this.responseHeaderDuration = okHttpCall.responseHeadersEnd - okHttpCall.responseHeadersStart;
    this.responseBodyDuration = okHttpCall.responseBodyEnd - okHttpCall.responseBodyStart;
    this.responseLatency = okHttpCall.responseHeadersStart - okHttpCall.requestBodyEnd;
  }

  void setTotalDuration(long totalDuration) {
    this.totalDuration = totalDuration;
  }

  void setClientLatency(long clientLatency) {
    this.clientLatency = clientLatency;
  }

  void setCallStatus(boolean isSuccessful) {
    this.isSuccessful = isSuccessful;
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

  long getClientLatency() {
    return this.clientLatency;
  }

  long getTotalDuration() {
    return this.totalDuration;
  }

  boolean isSuccessful() {
    return this.isSuccessful;
  }
}
