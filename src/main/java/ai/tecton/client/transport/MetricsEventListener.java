package ai.tecton.client.transport;

import okhttp3.*;

class MetricsEventListener extends EventListener {
  long callStart;
  long callEnd;
  long requestHeadersStart;
  long requestHeadersEnd;
  long requestBodyStart;
  long requestBodyEnd;
  long responseHeadersStart;
  long responseHeadersEnd;
  long responseBodyStart;
  long responseBodyEnd;
  long connectionAcquired;
  long connectionReleased;

  @Override
  public void callStart(Call call) {
    printEvent("callStart");
    this.callStart = System.currentTimeMillis();
  }

  @Override
  public void requestHeadersStart(Call call) {
    printEvent("requestHeaderStart");
    this.requestHeadersStart = System.currentTimeMillis();
  }

  @Override
  public void requestHeadersEnd(Call call, Request request) {
    printEvent("requestHeaderEnd");
    this.requestHeadersEnd = System.currentTimeMillis();
  }

  @Override
  public void requestBodyStart(Call call) {
    printEvent("requestBodyStart");
    this.requestBodyStart = System.currentTimeMillis();
  }

  @Override
  public void requestBodyEnd(Call call, long byteCount) {
    printEvent("requestBodyEnd");
    this.requestBodyEnd = System.currentTimeMillis();
  }

  @Override
  public void responseHeadersStart(Call call) {
    printEvent("responseHeaderStart");
    this.responseHeadersStart = System.currentTimeMillis();
  }

  @Override
  public void responseHeadersEnd(Call call, Response response) {
    printEvent("responseHeaderEnd");
    this.responseHeadersEnd = System.currentTimeMillis();
  }

  @Override
  public void responseBodyStart(Call call) {
    printEvent("responseBodyStart");
    this.responseBodyStart = System.currentTimeMillis();
  }

  @Override
  public void responseBodyEnd(Call call, long byteCount) {
    printEvent("responseBodyEnd");
    this.responseBodyEnd = System.currentTimeMillis();
  }

  @Override
  public void callEnd(Call call) {
    printEvent("callEnd");
    this.callEnd = System.currentTimeMillis();
  }

  @Override
  public void connectionAcquired(Call call, Connection connection) {
    printEvent("connectionAcquired");
    this.connectionAcquired = System.currentTimeMillis();
  }

  @Override
  public void connectionReleased(Call call, Connection connection) {
    printEvent("connectionreleased");
    this.connectionReleased = System.currentTimeMillis();
  }

  HttpMetrics build() {
    return new HttpMetrics()
        .setTotalCallDuration(this.callEnd - this.callStart)
        .setRequestHeaderDuration(this.requestHeadersEnd - this.requestHeadersStart)
        .setRequestBodyDuration(this.requestBodyEnd - this.requestBodyStart)
        .setResponseHeaderDuration(this.responseHeadersEnd - this.responseHeadersStart)
        .setResponseBodyDuration(this.responseBodyEnd - this.responseBodyStart)
        .setResponseLatency(this.responseHeadersStart - this.requestBodyEnd)
        .setSuccessfulConnectionTime(this.connectionReleased - this.connectionAcquired);
  }

  private void printEvent(String name) {
    // System.out.printf("%04d %s\n", System.currentTimeMillis() % 10000, name);
  }
}
