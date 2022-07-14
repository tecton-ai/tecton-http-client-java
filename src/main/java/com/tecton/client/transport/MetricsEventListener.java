package com.tecton.client.transport;

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
    this.callStart = System.currentTimeMillis();
  }

  @Override
  public void requestHeadersStart(Call call) {
    this.requestHeadersStart = System.currentTimeMillis();
  }

  @Override
  public void requestHeadersEnd(Call call, Request request) {
    this.requestHeadersEnd = System.currentTimeMillis();
  }

  @Override
  public void requestBodyStart(Call call) {
    this.requestBodyStart = System.currentTimeMillis();
  }

  @Override
  public void requestBodyEnd(Call call, long byteCount) {
    this.requestBodyEnd = System.currentTimeMillis();
  }

  @Override
  public void responseHeadersStart(Call call) {
    this.responseHeadersStart = System.currentTimeMillis();
  }

  @Override
  public void responseHeadersEnd(Call call, Response response) {
    this.responseHeadersEnd = System.currentTimeMillis();
  }

  @Override
  public void responseBodyStart(Call call) {
    this.responseBodyStart = System.currentTimeMillis();
  }

  @Override
  public void responseBodyEnd(Call call, long byteCount) {
    this.responseBodyEnd = System.currentTimeMillis();
  }

  @Override
  public void callEnd(Call call) {
    this.callEnd = System.currentTimeMillis();
  }

  @Override
  public void connectionAcquired(Call call, Connection connection) {
    this.connectionAcquired = System.currentTimeMillis();
  }

  @Override
  public void connectionReleased(Call call, Connection connection) {
    this.connectionReleased = System.currentTimeMillis();
  }

  HttpMetrics build() {
    return new HttpMetrics()
        .setTotalCallDuration(this.callEnd - this.callStart)
        .setRequestHeaderDuration(this.requestHeadersEnd - this.requestHeadersStart)
        .setRequestBodyDuration(this.requestBodyEnd - this.requestBodyStart)
        .setResponseHeaderDuration(this.responseHeadersEnd - this.responseHeadersStart)
        .setResponseBodyDuration(this.responseBodyEnd - this.responseBodyStart)
        .setSuccessfulConnectionTime(this.connectionReleased - this.connectionAcquired);
  }
}
