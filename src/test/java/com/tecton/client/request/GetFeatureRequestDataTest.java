package com.tecton.client.request;

import com.tecton.client.exceptions.TectonErrorMessage;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.fail;

public class GetFeatureRequestDataTest {

  GetFeaturesRequestData getFeaturesRequestData;

  @Before
  public void setup() {
    getFeaturesRequestData = new GetFeaturesRequestData();
  }

  @Test
  public void testNullJoinKey() {
    try {
      getFeaturesRequestData.addJoinKey(null, "testValue");
      fail();
    } catch (NullPointerException e) {
      Assert.assertEquals(TectonErrorMessage.INVALID_KEY_VALUE, e.getMessage());
    }
  }

  @Test
  public void testNullJoinValue() {
    try {
      getFeaturesRequestData.addJoinKey("testKey", (String) null);
      fail();
    } catch (NullPointerException e) {
      Assert.assertEquals(TectonErrorMessage.INVALID_KEY_VALUE, e.getMessage());
    }
  }

  @Test
  public void testNullRequestContextKey() {
    try {
      getFeaturesRequestData.addRequestContext(null, "testValue");
      fail();
    } catch (NullPointerException e) {
      Assert.assertEquals(TectonErrorMessage.INVALID_KEY_VALUE, e.getMessage());
    }
  }

  @Test
  public void testNullRequestContextValue() {
    try {
      getFeaturesRequestData.addRequestContext("testKey", (String) null);
      fail();
    } catch (NullPointerException e) {
      Assert.assertEquals(TectonErrorMessage.INVALID_KEY_VALUE, e.getMessage());
    }
  }

  @Test
  public void testEmptyJoinKey() {
    try {
      getFeaturesRequestData.addJoinKey("", "testValue");
      fail();
    } catch (IllegalArgumentException e) {
      Assert.assertEquals(TectonErrorMessage.INVALID_KEY_VALUE, e.getMessage());
    }
  }

  @Test
  public void testEmptyJoinValue() {
    try {
      getFeaturesRequestData.addJoinKey("testKey", "");
      fail();
    } catch (IllegalArgumentException e) {
      Assert.assertEquals(TectonErrorMessage.INVALID_KEY_VALUE, e.getMessage());
    }
  }

  @Test
  public void testEmptyRequestContextKey() {
    try {
      getFeaturesRequestData.addRequestContext("", "testValue");
      fail();
    } catch (IllegalArgumentException e) {
      Assert.assertEquals(TectonErrorMessage.INVALID_KEY_VALUE, e.getMessage());
    }
  }

  @Test
  public void testEmptyRequestContextValue() {
    try {
      getFeaturesRequestData.addRequestContext("testKey", "");
      fail();
    } catch (IllegalArgumentException e) {
      Assert.assertEquals(TectonErrorMessage.INVALID_KEY_VALUE, e.getMessage());
    }
  }

  @Test
  public void testMixedTypeJoinKeyValues() {
    getFeaturesRequestData
        .addJoinKey("testStringKey", "testValue")
        .addJoinKey("testLongKey", 1234L);

    Map<String, String> joinKeyMap = getFeaturesRequestData.getJoinKeyMap();
    Assert.assertNotNull(joinKeyMap);
    Assert.assertEquals(2, joinKeyMap.size());
    Assert.assertEquals(joinKeyMap.get("testStringKey"), "testValue");
    Assert.assertEquals(joinKeyMap.get("testLongKey"), "1234");
  }

  @Test
  public void testMixedTypeRequestContextKeyValues() {
    getFeaturesRequestData
        .addRequestContext("testStringKey", "testStringValue")
        .addRequestContext("testLongKey", 1234L)
        .addRequestContext("testDoubleKey", 125.55);

    Map<String, Object> requestContextMap = getFeaturesRequestData.getRequestContextMap();
    Assert.assertNotNull(requestContextMap);
    Assert.assertEquals(3, requestContextMap.size());
    Assert.assertEquals(requestContextMap.get("testStringKey"), "testStringValue");
    Assert.assertEquals(requestContextMap.get("testLongKey"), "1234");
    Assert.assertEquals(requestContextMap.get("testDoubleKey"), 125.55);
  }

  @Test
  public void testJoinKeyAndRequestContext() {
    Map<String, String> joinKeyMap =
        new HashMap<String, String>() {
          {
            put("testJoinKey1", "testJoinValue1");
            put("testJoinKey2", "testJoinValue2");
          }
        };

    Map<String, Object> requestContextMap =
        new HashMap<String, Object>() {
          {
            put("testRequestContext1", 555.55);
            put("testRequestContext2", "testStringValue");
          }
        };

    getFeaturesRequestData.addJoinKeyMap(joinKeyMap).addRequestContextMap(requestContextMap);

    Assert.assertEquals(2, getFeaturesRequestData.getRequestContextMap().size());
    Assert.assertEquals(2, getFeaturesRequestData.getJoinKeyMap().size());
    joinKeyMap
        .keySet()
        .forEach(
            key -> {
              Assert.assertEquals(
                  joinKeyMap.get(key), getFeaturesRequestData.getJoinKeyMap().get(key));
            });

    requestContextMap
        .keySet()
        .forEach(
            key -> {
              Assert.assertEquals(
                  requestContextMap.get(key),
                  getFeaturesRequestData.getRequestContextMap().get(key));
            });
  }
}
