package ai.tecton.client.model;

import org.apache.commons.lang3.StringUtils;

/**
 * Enum representing options for different metadata information that can be requested from the
 * FeatureService API
 */
public enum MetadataOption {
  /** Include feature name in the response */
  NAME("include_names"),
  /** Include feature effective_time in the response */
  EFFECTIVE_TIME("include_effective_times"),
  /** Include feature data_type in the response. */
  DATA_TYPE("include_data_types"),
  /** Include SLO Info in the response */
  SLO_INFO("include_slo_info"),
  /** Include all metadata in the response */
  ALL(),
  /**
   * Include no metadata in the response. Note that the default metadata options - NAME and
   * DATA_TYPE will still be included
   */
  NONE();

  private final String jsonName;

  MetadataOption() {
    this.jsonName = StringUtils.EMPTY;
  }

  MetadataOption(String name) {
    this.jsonName = name;
  }

  public String getJsonName() {
    return jsonName;
  }
}
