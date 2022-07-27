package ai.tecton.client.model;

import java.util.Arrays;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

/** Enum that represents the different data types (primitive or custom) of feature values */
public enum ValueType {
  /** java.lang.Boolean */
  BOOLEAN("boolean"),
  /** java.lang.Long */
  INT64("int64"),
  /** java.lang.String */
  STRING("string"),
  /** java.lang.Float */
  FLOAT32("float32"),
  /** java.lang.Double */
  FLOAT64("float64"),
  /** java.util.List */
  ARRAY("array");

  final String name;

  ValueType(String name) {
    this.name = name;
  }

  String getName() {
    return this.name;
  }

  /**
   * Returns the ValueType that matches the String representation passed as a parameter
   *
   * @param name The String representation of the ValueType
   * @return Optional&lt;{@link ValueType}&gt; if a match is found, Optional.empty() otherwise
   */
  public static Optional<ValueType> fromString(String name) {
    // Map string to the corresponding ValueType enum
    return Arrays.stream(ValueType.values())
        .filter(val -> StringUtils.equalsIgnoreCase(val.getName(), name))
        .findAny();
  }
}
