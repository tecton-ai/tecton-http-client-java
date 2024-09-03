package ai.tecton.client.response;

import ai.tecton.client.exceptions.TectonClientException;
import ai.tecton.client.exceptions.TectonErrorMessage;
import ai.tecton.client.model.FeatureStatus;
import ai.tecton.client.model.FeatureValue;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

class GetFeaturesResponseUtils {

  private static final String NAME = "Name";
  private static final String DATA_TYPE = "Data Type";

  // Construct Feature Vector from list of object and metadata
  static List<FeatureValue> constructFeatureVector(
      List<Object> features, List<FeatureMetadata> featureMetadata, int index) {
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
              (featureMetadata.get(i).status != null)
                  ? FeatureStatus.fromString(featureMetadata.get(i).status.get(index))
                  : null,
              featureMetadata.get(i).description,
              featureMetadata.get(i).tags);
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
    List<String> status;
    String description;
    Map<String, String> tags;
  }

  static class FeatureVectorJson {
    List<Object> features;
  }
}
