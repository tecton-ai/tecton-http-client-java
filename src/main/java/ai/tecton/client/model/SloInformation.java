package ai.tecton.client.model;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Class that represents SLO Info provided by Tecton when serving feature values. All values
 * returned are wrapped in {@link }
 */
public class SloInformation {

  private Boolean sloEligible;

  private Double serverTimeSeconds;

  private Double sloServerTimeSeconds;

  private Integer storeResponseSizeBytes;

  private Set<SloIneligibilityReason> sloIneligibilityReasons;

  private Double storeMaxLatency;

  public SloInformation(
      Boolean isSloEligible,
      Double serverTimeSeconds,
      Double sloServerTimeSeconds,
      Integer storeResponseSizeBytes,
      Set<SloIneligibilityReason> sloIneligibilityReasons,
      Double storeMaxLatency) {
    this.sloEligible = isSloEligible;
    this.serverTimeSeconds = serverTimeSeconds;
    this.sloServerTimeSeconds = sloServerTimeSeconds;
    this.sloIneligibilityReasons = sloIneligibilityReasons;
    this.storeResponseSizeBytes = storeResponseSizeBytes;
    this.storeMaxLatency = storeMaxLatency;
  }

  /**
   * Returns true if the response was eligible for SLO, false otherwise.
   *
   * @return Optional&lt;Boolean&gt; if present, Optional.empty() otherwise
   */
  public Optional<Boolean> isSloEligible() {
    return Optional.ofNullable(sloEligible);
  }

  /**
   * Reasons for the response not being eligible for SLO.
   *
   * @return List&lt;{@link SloInformation.SloIneligibilityReason}
   */
  public Set<SloIneligibilityReason> getSloIneligibilityReasons() {
    return sloIneligibilityReasons == null ? Collections.emptySet() : sloIneligibilityReasons;
  }

  /**
   * This includes the total time spent in the feature server including online transforms and store
   * latency
   *
   * @return Optional&lt;Double&gt; if present, Optional.empty() otherwise
   */
  public Optional<Double> getServerTimeSeconds() {
    return Optional.ofNullable(serverTimeSeconds);
  }

  /**
   * Max latency observed by the request from the store in seconds
   *
   * @return Optional&lt;Double&gt; if present, Optional.empty() otherwise
   */
  public Optional<Double> getStoreMaxLatency() {
    return Optional.ofNullable(storeMaxLatency);
  }

  /**
   * Total store response size bytes
   *
   * @return Optional&lt;Integer&gt; if present, Optional.empty() otherwise
   */
  public Optional<Integer> getStoreResponseSizeBytes() {
    return Optional.ofNullable(storeResponseSizeBytes);
  }

  /**
   * The server time minus any time spent waiting on line transforms to finish after all table
   * transforms have finished.
   *
   * @return Optional&lt;Double&gt; if present, Optional.empty() otherwise
   */
  public Optional<Double> getSloServerTimeSeconds() {
    return Optional.ofNullable(sloServerTimeSeconds);
  }

  /** Reasons due to which the Feature Serving Response can be ineligible for SLO */
  public enum SloIneligibilityReason {
    UNKNOWN,
    DYNAMODB_RESPONSE_SIZE_LIMIT_EXCEEDED,
    REDIS_RESPONSE_SIZE_LIMIT_EXCEEDED,
    REDIS_LATENCY_LIMIT_EXCEEDED;
  }

  // A static builder for SloInformation
  public static class Builder {
    Boolean isSloEligible;
    Double serverTimeSeconds;
    Double sloServerTimeSeconds;
    Integer storeResponseSizeByte;
    Set<SloIneligibilityReason> sloIneligibilityReasons;
    Double storeMaxLatency;

    public Builder isSloEligible(boolean isSloEligible) {
      this.isSloEligible = isSloEligible;
      return this;
    }

    public Builder serverTimeSeconds(Double serverTimeSeconds) {
      this.serverTimeSeconds = serverTimeSeconds;
      return this;
    }

    public Builder sloServerTimeSeconds(Double sloServerTimeSeconds) {
      this.sloServerTimeSeconds = sloServerTimeSeconds;
      return this;
    }

    public Builder storeResponseSizeBytes(Integer storeResponseSizeByte) {
      this.storeResponseSizeByte = storeResponseSizeByte;
      return this;
    }

    public Builder sloIneligibilityReasons(Set<SloIneligibilityReason> sloIneligibilityReasons) {
      this.sloIneligibilityReasons = sloIneligibilityReasons;
      return this;
    }

    public Builder storeMaxLatency(Double storeMaxLatency) {
      this.storeMaxLatency = storeMaxLatency;
      return this;
    }

    public SloInformation build() {
      return new SloInformation(
          this.isSloEligible,
          this.serverTimeSeconds,
          this.sloServerTimeSeconds,
          this.storeResponseSizeByte,
          this.sloIneligibilityReasons,
          this.storeMaxLatency);
    }
  }

  /** Overrides <i>equals()</i> in class {@link Object} */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SloInformation that = (SloInformation) o;
    return Objects.equals(sloEligible, that.sloEligible)
        && Objects.equals(serverTimeSeconds, that.serverTimeSeconds)
        && Objects.equals(sloServerTimeSeconds, that.sloServerTimeSeconds)
        && Objects.equals(storeResponseSizeBytes, that.storeResponseSizeBytes)
        && Objects.equals(sloIneligibilityReasons, that.sloIneligibilityReasons)
        && Objects.equals(storeMaxLatency, that.storeMaxLatency);
  }

  /** Overrides <i>hashCode()</i> in class {@link Object} */
  @Override
  public int hashCode() {
    return Objects.hash(
        sloEligible,
        serverTimeSeconds,
        sloServerTimeSeconds,
        storeResponseSizeBytes,
        sloIneligibilityReasons,
        storeMaxLatency);
  }
}
