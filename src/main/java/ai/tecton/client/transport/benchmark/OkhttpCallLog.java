package ai.tecton.client.transport.benchmark;

public class OkhttpCallLog {
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

  public boolean isValidCallLog() {
    return callStart != 0 && requestHeadersStart != 0 && requestHeadersEnd != 0;
  }
}
