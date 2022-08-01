package ai.tecton.client.transport.benchmark;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import okhttp3.*;

// Extend Okhttp's EventListener class to create an OkHttpCallLog
public class CallEventListener extends EventListener {

  long callId;
  public static Map<Long, OkhttpCallLog> callToMetricsMap = new HashMap<>();

  public static final Factory MetricsEventListenerFactory =
      new Factory() {
        final AtomicLong nextCallId = new AtomicLong(1L);

        @Override
        public CallEventListener create(Call Call) {
          Long callId = nextCallId.getAndIncrement();
          return new CallEventListener(callId);
        }
      };

  CallEventListener(Long callId) {
    this.callId = callId;
    OkhttpCallLog metric = new OkhttpCallLog();
    callToMetricsMap.put(callId, metric);
  }

  @Override
  public void callStart(Call call) {
    if (!callToMetricsMap.containsKey(callId)) {
      OkhttpCallLog metric = new OkhttpCallLog();
      callToMetricsMap.put(callId, metric);
    }
    callToMetricsMap.get(callId).callStart = System.currentTimeMillis();
  }

  @Override
  public void requestHeadersStart(Call call) {
    if (callToMetricsMap.containsKey(callId))
      callToMetricsMap.get(callId).requestHeadersStart = System.currentTimeMillis();
  }

  @Override
  public void requestHeadersEnd(Call call, Request request) {

    if (callToMetricsMap.containsKey(callId))
      callToMetricsMap.get(callId).requestHeadersEnd = System.currentTimeMillis();
  }

  @Override
  public void requestBodyStart(Call call) {
    if (callToMetricsMap.containsKey(callId))
      callToMetricsMap.get(callId).requestBodyStart = System.currentTimeMillis();
  }

  @Override
  public void requestBodyEnd(Call call, long byteCount) {
    if (callToMetricsMap.containsKey(callId))
      callToMetricsMap.get(callId).requestBodyEnd = System.currentTimeMillis();
  }

  @Override
  public void responseHeadersStart(Call call) {
    if (callToMetricsMap.containsKey(callId))
      callToMetricsMap.get(callId).responseHeadersStart = System.currentTimeMillis();
  }

  @Override
  public void responseHeadersEnd(Call call, Response response) {
    if (callToMetricsMap.containsKey(callId))
      callToMetricsMap.get(callId).responseHeadersEnd = System.currentTimeMillis();
  }

  @Override
  public void responseBodyStart(Call call) {
    if (callToMetricsMap.containsKey(callId))
      callToMetricsMap.get(callId).responseBodyStart = System.currentTimeMillis();
  }

  @Override
  public void responseBodyEnd(Call call, long byteCount) {
    if (callToMetricsMap.containsKey(callId))
      callToMetricsMap.get(callId).responseBodyEnd = System.currentTimeMillis();
  }

  @Override
  public void callEnd(Call call) {
    if (callToMetricsMap.containsKey(callId))
      callToMetricsMap.get(callId).callEnd = System.currentTimeMillis();
  }

  public long getCallId() {
    return this.callId;
  }
}
