package ai.tecton.client.request;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Class that represents request-level options to control feature server behavior. These options are
 * sent as part of the requestOptions field in GetFeatures and GetFeaturesBatch requests. Option
 * values must be either Integer or Boolean.
 */
public class RequestOptions {

  private Map<String, Object> options;

  /** Constructor that creates a new RequestOptions object with default empty options */
  public RequestOptions() {
    this.options = new HashMap<>();
  }

  /**
   * Sets a request option with the given key and value.
   *
   * @param key the option key
   * @param value the option value (must be either Integer or Boolean)
   * @return Returns the RequestOptions object for method chaining
   * @throws IllegalArgumentException if value is not Integer or Boolean
   */
  public RequestOptions setOption(String key, Object value) {
    if (value != null && !(value instanceof Integer) && !(value instanceof Boolean)) {
      throw new IllegalArgumentException(
          "Option value must be either Integer or Boolean, got: "
              + value.getClass().getSimpleName());
    }
    this.options.put(key, value);
    return this;
  }

  /**
   * Gets a specific option value by key.
   *
   * @param key the option key
   * @return the option value, or null if not set
   */
  public Object getOption(String key) {
    return this.options.get(key);
  }

  /**
   * Gets a specific option value by key as an Integer.
   *
   * @param key the option key
   * @return the option value as Integer, or null if not set or not an Integer
   */
  public Integer getIntegerOption(String key) {
    Object value = this.options.get(key);
    return value instanceof Integer ? (Integer) value : null;
  }

  /**
   * Gets a specific option value by key as a Boolean.
   *
   * @param key the option key
   * @return the option value as Boolean, or null if not set or not a Boolean
   */
  public Boolean getBooleanOption(String key) {
    Object value = this.options.get(key);
    return value instanceof Boolean ? (Boolean) value : null;
  }

  /**
   * Gets all options as an unmodifiable map.
   *
   * @return Map containing all request options
   */
  public Map<String, Object> getOptions() {
    return new HashMap<>(this.options);
  }

  /**
   * Checks if any options are set.
   *
   * @return true if no options are set, false otherwise
   */
  public boolean isEmpty() {
    return this.options.isEmpty();
  }

  /** A Builder class for creating an instance of {@link RequestOptions} object */
  public static class Builder {
    private RequestOptions requestOptions;

    /** Instantiates a new Builder */
    public Builder() {
      requestOptions = new RequestOptions();
    }

    /**
     * Sets a request option with the given key and value.
     *
     * @param key the option key
     * @param value the option value (must be either Integer or Boolean)
     * @return this Builder
     * @throws IllegalArgumentException if value is not Integer or Boolean
     */
    public Builder option(String key, Object value) {
      requestOptions.setOption(key, value);
      return this;
    }

    /**
     * Build a {@link RequestOptions} object from the Builder
     *
     * @return {@link RequestOptions}
     */
    public RequestOptions build() {
      return this.requestOptions;
    }
  }

  /** Overrides <i>equals()</i> in class {@link Object} */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RequestOptions that = (RequestOptions) o;
    return Objects.equals(options, that.options);
  }

  /** Overrides <i>hashCode()</i> in class {@link Object} */
  @Override
  public int hashCode() {
    return Objects.hash(options);
  }

  @Override
  public String toString() {
    return "RequestOptions{" + "options=" + options + '}';
  }
}
