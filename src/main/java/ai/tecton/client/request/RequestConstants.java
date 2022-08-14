package ai.tecton.client.request;

import ai.tecton.client.model.MetadataOption;
import java.time.Duration;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

public final class RequestConstants {

  public static final int MAX_MICRO_BATCH_SIZE = 10;
  public static final int DEFAULT_MICRO_BATCH_SIZE = 1;
  public static final Duration NONE_TIMEOUT = Duration.ofMillis(Long.MAX_VALUE);
  public static Set<MetadataOption> DEFAULT_METADATA_OPTIONS =
      new HashSet<>(Arrays.asList(MetadataOption.NAME, MetadataOption.DATA_TYPE));
  public static Set<MetadataOption> ALL_METADATA_OPTIONS =
      EnumSet.complementOf(EnumSet.of(MetadataOption.ALL, MetadataOption.NONE));
  public static Set<MetadataOption> NONE_METADATA_OPTIONS = EnumSet.noneOf(MetadataOption.class);
}
