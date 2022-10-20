package ai.tecton.client.response;

import ai.tecton.client.exceptions.TectonClientException;
import ai.tecton.client.exceptions.TectonErrorMessage;
import ai.tecton.client.model.FeatureValue;
import ai.tecton.client.model.Status;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

class GetFeaturesResponseUtils {

  private static final String NAME = "Name";
  private static final String DATA_TYPE = "Data Type";

  // Construct Feature Vector from list of object and metadata
  static List<FeatureValue> constructFeatureVector(
      List<Object> features, List<FeatureMetadata> featureMetadata) {
    validateResponse(features, featureMetadata);
    List<FeatureValue> featureValues = new ArrayList<>(features.size());
    for (int i = 0; i < features.size(); i++) {
      FeatureValue value =
          new FeatureValue(
              features.get(i),
              featureMetadata.get(i).name,
              featureMetadata.get(i).dataType.getDataType(),
              featureMetadata.get(i).dataType.getListElementType(),
              featureMetadata.get(i).effectiveTime,
              Status.fromString(featureMetadata.get(i).status));
      featureValues.add(value);
    }
    return featureValues;
  }

  // Validate response from Feature Server
  static void validateResponse(List<Object> featureVector, List<FeatureMetadata> featureMetadata) {
    if (featureVector.isEmpty()) {
      throw new TectonClientException(TectonErrorMessage.EMPTY_FEATURE_VECTOR);
    }
    for (FeatureMetadata metadata : featureMetadata) {
      if (StringUtils.isEmpty(metadata.name)) {
        throw new TectonClientException(
            String.format(TectonErrorMessage.MISSING_EXPECTED_METADATA, NAME));
      }
      if (StringUtils.isEmpty(metadata.dataType.type)) {
        {
          throw new TectonClientException(
              String.format(TectonErrorMessage.MISSING_EXPECTED_METADATA, DATA_TYPE));
        }
      }
    }
  }

  // Common JSON response classes for Moshi serialization
  static class FeatureMetadata {
    String name;
    String effectiveTime;
    AbstractTectonResponse.ResponseDataType dataType =
        new AbstractTectonResponse.ResponseDataType();
    String status;
  }

  static class FeatureVectorJson {
    List<Object> features;
  }
}
