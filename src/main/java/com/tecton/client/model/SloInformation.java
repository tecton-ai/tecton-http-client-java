package com.tecton.client.model;

import java.util.List;
import java.util.Optional;

public class SloInformation {
  Boolean sloEligible;

  Double serverTimeSeconds;

  Double sloServerTimeSeconds;

  Integer storeResponseSizeBytes;

  List<SloIneligibilityReason> sloIneligibilityReasons;

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

  public Optional<Double> getSloServerTimeSeconds() {
    return Optional.ofNullable(sloServerTimeSeconds);
  }

  public enum SloIneligibilityReason {
    UNKNOWN,
    DYNAMODB_RESPONSE_SIZE_LIMIT_EXCEEDED,
    REDIS_RESPONSE_SIZE_LIMIT_EXCEEDED,
    REDIS_LATENCY_LIMIT_EXCEEDED;
  }
}
