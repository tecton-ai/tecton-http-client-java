package com.tecton.client.model;

import com.squareup.moshi.Json;
import java.util.List;
import java.util.Optional;

public class SloInformation {
  @Json(name = "slo_eligible")
  Boolean sloEligible;

  @Json(name = "server_time_seconds")
  Double serverTimeSeconds;

  @Json(name = "slo_server_time_seconds")
  Integer sloServerTimeSeconds;

  @Json(name = "store_response_size_bytes")
  Integer storeResponseSizeBytes;

  @Json(name = "slo_ineligibility_reasons")
  List<SloIneligibilityReason> sloIneligibilityReasons;

  @Json(name = "store_max_latency")
  Double storeMaxLatency;

  public Optional<Boolean> isSloEligible() {
    return Optional.ofNullable(sloEligible);
  }

  public List<SloIneligibilityReason> getSloIneligibilityReasons() {
    return sloIneligibilityReasons;
  }

  public Optional<Double> getServerTimeSeconds() {
    return Optional.ofNullable(serverTimeSeconds);
  }

  public Optional<Double> getStoreMaxLatency() {
    return Optional.ofNullable(storeMaxLatency);
  }

  public Optional<Integer> getStoreResponseSizeBytes() {
    return Optional.ofNullable(storeResponseSizeBytes);
  }

  public enum SloIneligibilityReason {
    UNKNOWN,
    DYNAMODB_RESPONSE_SIZE_LIMIT_EXCEEDED,
    REDIS_RESPONSE_SIZE_LIMIT_EXCEEDED,
    REDIS_LATENCY_LIMIT_EXCEEDED;
  }
}
