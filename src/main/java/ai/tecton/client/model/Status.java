package ai.tecton.client.model;

import java.util.Arrays;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

/**
 * Enum that represents the different feature statuses that can be returned when requesting feature
 * values.
 */
public enum Status {
  /** Feature value is present and returned with no errors. */
  PRESENT("PRESENT"),
  /**
   * Either the join keys requested are missing in the online store or the feature value is outside
   * ttl.
   */
  MISSING_DATA("MISSING_DATA"),
  /** Unable to infer feature status. */
  UNKNOWN("UNKNOWN");

  final String status;

  Status(String status) {
    this.status = status;
  }

  String getStatus() {
    return this.status;
  }

  /**
   * Returns the Status that matches the String representation passed as a parameter
   *
   * @param name The String representation of the Status
   * @return Optional&lt;{@link ValueType}&gt; if a match is found, Optional.empty() otherwise
   */
  public static Optional<Status> fromString(String name) {
    // Map string to the corresponding ValueType enum
    return Arrays.stream(Status.values())
        .filter(val -> StringUtils.equalsIgnoreCase(val.getStatus(), name))
        .findAny();
  }
}
