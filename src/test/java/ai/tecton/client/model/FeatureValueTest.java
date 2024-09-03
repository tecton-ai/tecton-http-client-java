package ai.tecton.client.model;

import static org.junit.Assert.fail;

import ai.tecton.client.exceptions.TectonClientException;
import ai.tecton.client.exceptions.TectonErrorMessage;

import java.util.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class FeatureValueTest {

  String testName;

  @Before
  public void setup() {
    this.testName = "test_fs_name_space.test_fs_name";
  }

  @Test
  public void testStringValue() {
    FeatureValue featureValue =
        new FeatureValue(
            "stringValue",
            testName,
            ValueType.STRING,
            Optional.empty(),
            null,
            Optional.ofNullable(FeatureStatus.PRESENT),
            null,
            null);
    Assert.assertEquals("test_fs_name_space", featureValue.getFeatureNamespace());
    Assert.assertEquals("test_fs_name", featureValue.getFeatureName());
    Assert.assertEquals(ValueType.STRING, featureValue.getValueType());
    Assert.assertEquals("stringValue", featureValue.stringValue());
  }

  @Test
  public void testFloat64Value() {
    FeatureValue featureValue =
        new FeatureValue(
            555.55,
            testName,
            ValueType.FLOAT64,
            Optional.empty(),
            null,
            Optional.ofNullable(FeatureStatus.PRESENT),
            null,
            null);

    Assert.assertEquals("test_fs_name_space", featureValue.getFeatureNamespace());
    Assert.assertEquals("test_fs_name", featureValue.getFeatureName());
    Assert.assertEquals(ValueType.FLOAT64, featureValue.getValueType());
    Assert.assertEquals(new Double(555.55), featureValue.float64Value());
  }

  @Test
  public void testFloat64ValueInvalidValues() {
    String[] invalidValues = {null, "NaN", "Infinity", "-Infinity"};
    for (String invalidValue : invalidValues) {
      FeatureValue featureValue =
          new FeatureValue(
              invalidValue,
              testName,
              ValueType.FLOAT64,
              Optional.empty(),
              null,
              Optional.ofNullable(FeatureStatus.PRESENT),
              null,
              null);

      Assert.assertEquals("test_fs_name_space", featureValue.getFeatureNamespace());
      Assert.assertEquals("test_fs_name", featureValue.getFeatureName());
      Assert.assertEquals(ValueType.FLOAT64, featureValue.getValueType());
      Assert.assertEquals(null, featureValue.float64Value());
    }
  }

  @Test
  public void testInt64Value() {

    FeatureValue featureValue =
        new FeatureValue(
            "0",
            testName,
            ValueType.INT64,
            Optional.empty(),
            null,
            Optional.ofNullable(FeatureStatus.PRESENT),
            null,
            null);

    Assert.assertEquals(ValueType.INT64, featureValue.getValueType());
    Assert.assertEquals(new Long(0), featureValue.int64value());
  }

  @Test
  public void testEffectiveTime() {
    FeatureValue featureValue =
        new FeatureValue(
            "testVal",
            testName,
            ValueType.STRING,
            Optional.empty(),
            "2021-08-21T01:23:58Z",
            Optional.ofNullable(FeatureStatus.PRESENT),
            null,
            null);

    Assert.assertEquals(ValueType.STRING, featureValue.getValueType());
    Assert.assertEquals("2021-08-21T01:23:58Z", featureValue.getEffectiveTime().get().toString());
  }

  @Test
  public void testStringList() {
    List<String> fruits = new ArrayList<>(Arrays.asList("apple", "mango", "kiwi", "orange"));
    FeatureValue featureValue =
        new FeatureValue(
            fruits,
            testName,
            ValueType.ARRAY,
            Optional.of(ValueType.STRING),
            null,
            Optional.ofNullable(FeatureStatus.PRESENT),
            null,
            null);

    Assert.assertEquals(ValueType.ARRAY, featureValue.getValueType());
    Assert.assertEquals(ValueType.STRING, featureValue.getListElementType().get());
    List<String> listValue = featureValue.stringArrayValue();
    Assert.assertEquals(4, listValue.size());
    Assert.assertEquals(fruits, listValue);
  }

  @Test
  public void testFloat32ListWithNulls() {
    List<Float> expectedArray =
        new ArrayList<Float>() {
          {
            add(2.5F);
            add(5.5F);
            add(null);
            add(null);
          }
        };

    FeatureValue featureValue =
        new FeatureValue(
            expectedArray,
            testName,
            ValueType.ARRAY,
            Optional.of(ValueType.FLOAT32),
            null,
            Optional.ofNullable(FeatureStatus.PRESENT),
            null,
            null);

    Assert.assertEquals(ValueType.ARRAY, featureValue.getValueType());
    Assert.assertEquals(ValueType.FLOAT32, featureValue.getListElementType().get());
    List<Float> actualArray = featureValue.float32ArrayValue();
    Assert.assertEquals(expectedArray, actualArray);
  }

  @Test
  public void testLongListWithNulls() {
    List<Long> expectedArray =
        new ArrayList<Long>() {
          {
            add(123L);
            add(335L);
            add(null);
            add(null);
          }
        };

    // The response from Tecton API will represent an array of Int64 values as an array of String
    List<String> arrayInput =
        new ArrayList<String>() {
          {
            add("123");
            add("335");
            add(null);
            add(null);
          }
        };

    FeatureValue featureValue =
        new FeatureValue(
            arrayInput,
            testName,
            ValueType.ARRAY,
            Optional.of(ValueType.INT64),
            null,
            Optional.of(FeatureStatus.PRESENT),
            null,
            null);

    Assert.assertEquals(ValueType.ARRAY, featureValue.getValueType());
    Assert.assertEquals(ValueType.INT64, featureValue.getListElementType().get());
    List<Long> actualArray = featureValue.int64ArrayValue();
    Assert.assertEquals(expectedArray, actualArray);
  }

  @Test
  public void testInvalidTypeAccess() {
    FeatureValue featureValue =
        new FeatureValue(
            "0",
            testName,
            ValueType.INT64,
            null,
            null,
            Optional.ofNullable(FeatureStatus.PRESENT),
            null,
            null);

    Assert.assertEquals(ValueType.INT64, featureValue.getValueType());
    try {
      Boolean boolVal = featureValue.booleanValue();
      fail();
    } catch (TectonClientException e) {
      String message = String.format(TectonErrorMessage.MISMATCHED_TYPE, "int64");
      Assert.assertEquals(message, e.getMessage());
    }

    try {
      List<Double> doubleListVal = featureValue.float64ArrayValue();
      fail();
    } catch (TectonClientException e) {
      String message = String.format(TectonErrorMessage.MISMATCHED_TYPE, "int64");
      Assert.assertEquals(message, e.getMessage());
    }
  }

  @Test
  public void testEqualsAndHashCode() {
    FeatureValue featureValue =
        new FeatureValue(
            "123",
            testName,
            ValueType.STRING,
            Optional.empty(),
            null,
            Optional.ofNullable(FeatureStatus.PRESENT),
            null,
            null);
    FeatureValue featureValueEquals =
        new FeatureValue(
            "123",
            testName,
            ValueType.STRING,
            Optional.empty(),
            null,
            Optional.ofNullable(FeatureStatus.PRESENT),
            null,
            null);
    FeatureValue featureValueNotEquals =
        new FeatureValue(
            "123",
            testName,
            ValueType.INT64,
            Optional.empty(),
            null,
            Optional.ofNullable(FeatureStatus.PRESENT),
            null,
            null);

    Assert.assertEquals(featureValue, featureValueEquals);
    Assert.assertEquals(featureValue.hashCode(), featureValueEquals.hashCode());

    Assert.assertNotEquals(featureValue, featureValueNotEquals);
    Assert.assertNotEquals(featureValue.hashCode(), featureValueNotEquals.hashCode());
  }

  @Test
  public void testFeatureDescriptionAndTags () {
    Map<String, String> tags = new HashMap<>();
    tags.put("tag", "value");
    FeatureValue featureValueHasDescriptionAndTags =
            new FeatureValue(
                    "123",
                    testName,
                    ValueType.STRING,
                    Optional.empty(),
                    null,
                    Optional.ofNullable(FeatureStatus.PRESENT),
                    "a description",
                    tags);
    FeatureValue featureValueDoesNotHaveDescriptionOrTags =
            new FeatureValue(
                    "123",
                    testName,
                    ValueType.INT64,
                    Optional.empty(),
                    null,
                    Optional.ofNullable(FeatureStatus.PRESENT),
                    null,
                    null);
    Assert.assertEquals(featureValueHasDescriptionAndTags.getFeatureDescription(), "a description");
    Assert.assertEquals(featureValueHasDescriptionAndTags.getFeatureTags(), tags);

    Assert.assertNull(featureValueDoesNotHaveDescriptionOrTags.getFeatureDescription());
    Assert.assertNull(featureValueDoesNotHaveDescriptionOrTags.getFeatureTags());
  }
}
