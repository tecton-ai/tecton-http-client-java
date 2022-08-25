package ai.tecton.client.request;

import ai.tecton.client.model.MetadataOption;
import java.time.Duration;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/**
 * Class that provides static constants that can be used as default parameters to various Request
 * objects
 */
public final class RequestConstants {
  /**
   * The maximum microBatchSize supported by Tecton for a {@link GetFeaturesBatchRequest}, set to
   * {@value MAX_MICRO_BATCH_SIZE}
   */
  public static final int MAX_MICRO_BATCH_SIZE = 5;
  /**
   * The default microBatchSize for a {@link GetFeaturesBatchRequest}, set to {@value
   * DEFAULT_MICRO_BATCH_SIZE}
   */
  public static final int DEFAULT_MICRO_BATCH_SIZE = 1;
  /** The default parameter for None timeout for a {@link GetFeaturesBatchRequest} */
  public static final Duration NONE_TIMEOUT = Duration.ofMillis(Long.MAX_VALUE);
  /**
   * The default set of MetadataOptions for a {@link GetFeaturesRequest} and a {@link
   * GetFeaturesBatchRequest}, includes {@link MetadataOption#NAME} and {@link
   * MetadataOption#DATA_TYPE}
   */
  public static Set<MetadataOption> DEFAULT_METADATA_OPTIONS =
      new HashSet<>(Arrays.asList(MetadataOption.NAME, MetadataOption.DATA_TYPE));

  /**
   * The set of all {@link MetadataOption} for a {@link GetFeaturesRequest} and a {@link
   * GetFeaturesBatchRequest}
   */
  public static Set<MetadataOption> ALL_METADATA_OPTIONS =
      EnumSet.complementOf(EnumSet.of(MetadataOption.ALL, MetadataOption.NONE));

  /**
   * An empty set representing None MetadataOptions for a {@link GetFeaturesRequest} and a {@link
   * GetFeaturesBatchRequest}
   */
  public static Set<MetadataOption> NONE_METADATA_OPTIONS = EnumSet.noneOf(MetadataOption.class);
}
