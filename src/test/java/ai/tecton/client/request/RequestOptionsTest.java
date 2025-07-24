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
  public void testBuilder() {
    RequestOptions options =
        new RequestOptions.Builder()
            .option("latency_budget_ms", 2000)
            .option("readFromCache", true)
            .option("writeFromCache", false)
            .build();

    assertEquals(2000, options.getOption("latency_budget_ms"));
    assertEquals(true, options.getOption("readFromCache"));
    assertEquals(false, options.getOption("writeFromCache"));
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
