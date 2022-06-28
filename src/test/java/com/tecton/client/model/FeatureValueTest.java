package com.tecton.client.model;

import com.tecton.client.exceptions.TectonClientException;
import com.tecton.client.exceptions.TectonErrorMessage;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;

import static org.junit.Assert.fail;

public class FeatureValueTest {

  String testName;

  @Before
  public void setup() {
    this.testName = "test_fs_name_space.test_fs_name";
  }

  @Test
  public void testStringValue() {
    FeatureValue featureValue = new FeatureValue("stringValue", testName, "string", null, null);
    Assert.assertEquals("test_fs_name_space", featureValue.getFeatureNamespace());
    Assert.assertEquals("test_fs_name", featureValue.getFeatureName());
    Assert.assertEquals(ValueType.STRING, featureValue.getValueType());
    Assert.assertEquals("stringValue", featureValue.stringValue());
  }

  @Test
  public void testFloat64Value() {
    FeatureValue featureValue = new FeatureValue(555.55, testName, "float64", null, null);
    Assert.assertEquals("test_fs_name_space", featureValue.getFeatureNamespace());
    Assert.assertEquals("test_fs_name", featureValue.getFeatureName());
    Assert.assertEquals(ValueType.FLOAT64, featureValue.getValueType());
    Assert.assertEquals(new Double(555.55), featureValue.float64Value());
  }

  @Test
  public void testInt64Value() {
    FeatureValue featureValue = new FeatureValue("0", testName, "int64", null, null);
    Assert.assertEquals(ValueType.INT64, featureValue.getValueType());
    Assert.assertEquals(new Long(0), featureValue.int64value());
  }

  @Test
  public void testEffectiveTime() throws ParseException {
    FeatureValue featureValue =
        new FeatureValue("testVal", testName, "string", null, "2021-08-21T01:23:58.996Z");
    Assert.assertEquals(ValueType.STRING, featureValue.getValueType());
    Assert.assertEquals("2021-08-21T01:23:58.996Z", featureValue.getEffectiveTime().toString());
  }

  @Test
  public void testInvalidTypeAccess() {
    FeatureValue featureValue = new FeatureValue("0", testName, "int64", null, null);
    Assert.assertEquals(ValueType.INT64, featureValue.getValueType());
    try {
      Boolean boolVal = featureValue.booleanValue();
      fail();
    } catch (TectonClientException e) {
      String message = String.format(TectonErrorMessage.MISMATCHED_TYPE, "int64");
      Assert.assertEquals(message, e.getMessage());
    }
  }
}
