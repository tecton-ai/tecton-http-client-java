package ai.tecton.client.request;

import static org.junit.Assert.*;

import org.junit.Test;

public class RequestOptionsTest {

  @Test
  public void testRequestOptionsCreation() {
    RequestOptions options = new RequestOptions();
    assertTrue(options.isEmpty());
  }

  @Test
  public void testIntegerOptionSetting() {
    RequestOptions options = new RequestOptions();
    options.setOption("latency_budget_ms", 1000);

    assertEquals(Integer.valueOf(1000), options.getIntegerOption("latency_budget_ms"));
    assertEquals(Integer.valueOf(1000), options.getOption("latency_budget_ms"));
    assertFalse(options.isEmpty());
    assertEquals(1, options.getOptions().size());
  }

  @Test
  public void testBooleanOptionSetting() {
    RequestOptions options = new RequestOptions();
    options.setOption("coerceNullCountsToZero", true);

    assertEquals(Boolean.TRUE, options.getBooleanOption("coerceNullCountsToZero"));
    assertEquals(Boolean.TRUE, options.getOption("coerceNullCountsToZero"));
    assertFalse(options.isEmpty());
  }

  @Test
  public void testMultipleBooleanOptions() {
    RequestOptions options = new RequestOptions();
    options.setOption("readFromCache", true);
    options.setOption("writeFromCache", false);
    options.setOption("ignoreExtraRequestContextFields", true);

    assertEquals(Boolean.TRUE, options.getBooleanOption("readFromCache"));
    assertEquals(Boolean.FALSE, options.getBooleanOption("writeFromCache"));
    assertEquals(Boolean.TRUE, options.getBooleanOption("ignoreExtraRequestContextFields"));
    assertEquals(3, options.getOptions().size());
  }

  @Test
  public void testMixedIntegerAndBooleanOptions() {
    RequestOptions options = new RequestOptions();
    options.setOption("latency_budget_ms", 5000);
    options.setOption("coerceNullCountsToZero", false);

    assertEquals(Integer.valueOf(5000), options.getIntegerOption("latency_budget_ms"));
    assertEquals(Boolean.FALSE, options.getBooleanOption("coerceNullCountsToZero"));
    assertEquals(2, options.getOptions().size());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidValueTypeThrowsException() {
    RequestOptions options = new RequestOptions();
    options.setOption("invalid_option", "string_value");
  }

  @Test
  public void testBuilder() {
    RequestOptions options =
        new RequestOptions.Builder()
            .option("latency_budget_ms", 2000)
            .option("readFromCache", true)
            .option("writeFromCache", false)
            .build();

    assertEquals(Integer.valueOf(2000), options.getIntegerOption("latency_budget_ms"));
    assertEquals(Boolean.TRUE, options.getBooleanOption("readFromCache"));
    assertEquals(Boolean.FALSE, options.getBooleanOption("writeFromCache"));
    assertEquals(3, options.getOptions().size());
  }

  @Test
  public void testEqualsAndHashCode() {
    RequestOptions options1 = new RequestOptions();
    options1.setOption("latency_budget_ms", 1000);
    options1.setOption("readFromCache", true);

    RequestOptions options2 = new RequestOptions();
    options2.setOption("latency_budget_ms", 1000);
    options2.setOption("readFromCache", true);

    assertEquals(options1, options2);
    assertEquals(options1.hashCode(), options2.hashCode());
  }
}
