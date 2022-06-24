package com.tecton.client.model;

import com.tecton.client.exceptions.TectonClientException;
import com.tecton.client.exceptions.TectonErrorMessage;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.util.*;

import static org.junit.Assert.fail;

public class FeatureValueTest {

  String testName;
  Map<String, String> dataType;

  @Before
  public void setup() {
    this.dataType = new HashMap<>();
    this.testName = "test_fs_name_space.test_fs_name";
  }

  @Test
  public void testStringValue() {
    dataType.put("type", "string");
    FeatureValue featureValue = new FeatureValue("stringValue", testName, dataType, null);
    Assert.assertEquals("test_fs_name_space", featureValue.getFeatureNamespace());
    Assert.assertEquals("test_fs_name", featureValue.getFeatureName());
    Assert.assertEquals(FeatureValue.ValueType.STRING, featureValue.getValueType());
    Assert.assertEquals("stringValue", featureValue.stringValue());
  }

  @Test
  public void testFloat64Value() {
    dataType.put("type", "float64");
    FeatureValue featureValue = new FeatureValue(555.55, testName, dataType, null);
    Assert.assertEquals("test_fs_name_space", featureValue.getFeatureNamespace());
    Assert.assertEquals("test_fs_name", featureValue.getFeatureName());
    Assert.assertEquals(FeatureValue.ValueType.FLOAT64, featureValue.getValueType());
    Assert.assertEquals(new Double(555.55), featureValue.float64Value());
  }

  @Test
  public void testInt64Value() {
    dataType.put("type", "int64");
    FeatureValue featureValue = new FeatureValue("0", testName, dataType, null);
    Assert.assertEquals(FeatureValue.ValueType.INT64, featureValue.getValueType());
    Assert.assertEquals(new Long(0), featureValue.int64value());
  }

  @Test
  public void testEffectiveTime() throws ParseException {
    dataType.put("type", "string");
    FeatureValue featureValue =
        new FeatureValue("testVal", testName, dataType, "2021-08-21T01:23:58.996Z");
    Assert.assertEquals(FeatureValue.ValueType.STRING, featureValue.getValueType());
    Assert.assertEquals("2021-08-21T01:23:58.996Z", featureValue.getEffectiveTime().toString());
  }

  @Test
  public void testInvalidTypeAccess() {
    dataType.put("type", "int64");
    FeatureValue featureValue = new FeatureValue("0", testName, dataType, null);
    Assert.assertEquals(FeatureValue.ValueType.INT64, featureValue.getValueType());
    try {
      Boolean boolVal = featureValue.booleanValue();
      fail();
    } catch (TectonClientException e) {
      String message = String.format(TectonErrorMessage.MISMATCHED_TYPE, "int64");
      Assert.assertEquals(message, e.getMessage());
    }
  }
}
