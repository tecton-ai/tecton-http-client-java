package ai.tecton.client.model;

import java.util.Collections;
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
  public static SloInformation buildSloInformation(
      Boolean isSloEligible,
      Double serverTimeSeconds,
      Double sloServerTimeSeconds,
      Integer storeResponseSizeBytes,
      Set<SloIneligibilityReason> sloIneligibilityReasons,
      Double storeMaxLatency) {
    SloInformation sloInformation = new SloInformation();
    sloInformation.sloEligible = isSloEligible;
    sloInformation.serverTimeSeconds = serverTimeSeconds;
    sloInformation.sloServerTimeSeconds = sloServerTimeSeconds;
    sloInformation.sloIneligibilityReasons = sloIneligibilityReasons;
    sloInformation.storeResponseSizeBytes = storeResponseSizeBytes;
    sloInformation.storeMaxLatency = storeMaxLatency;
    return sloInformation;
  }
}
