package ai.tecton.client.model;

import java.util.Arrays;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

/** Enum that represents the different data types (primitive or custom) of feature values */
public enum Status {
  /** java.lang.Boolean */
  PRESENT("PRESENT"),
  /** java.lang.Long */
  MISSING_DATA("MISSING_DATA"),
  /** java.lang.String */
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
