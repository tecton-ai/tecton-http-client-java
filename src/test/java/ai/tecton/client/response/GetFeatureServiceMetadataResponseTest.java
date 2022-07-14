package ai.tecton.client.response;

import ai.tecton.client.model.FeatureServiceMetadata;
import ai.tecton.client.model.NameAndType;
import ai.tecton.client.model.ValueType;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GetFeatureServiceMetadataResponseTest {

  GetFeatureServiceMetadataResponse featureServiceMetadataResponse;
  String metadataResponse;

  @Before
  public void setup() throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    String metadataInput =
        classLoader.getResource("response/sample_metadata_response.json").getFile();
    metadataResponse = new String(Files.readAllBytes(Paths.get(metadataInput)));
  }

  @Test
  public void testSimpleMetadataResponse() {
    featureServiceMetadataResponse =
        new GetFeatureServiceMetadataResponse(metadataResponse, Duration.ofMillis(5));
    Duration requestLatency = featureServiceMetadataResponse.getRequestLatency();
    Assert.assertEquals(Duration.ofMillis(5), requestLatency);
    FeatureServiceMetadata featureServiceMetadata =
        featureServiceMetadataResponse.getFeatureServiceMetadata();
    checkInputKeys(featureServiceMetadata.getInputJoinKeysAsMap());
    checkFeatureValues(featureServiceMetadata.getFeatureValuesAsMap());
  }

  private void checkInputKeys(Map<String, NameAndType> inputKeys) {
    Assert.assertEquals(2, inputKeys.size());
    Assert.assertEquals(ValueType.FLOAT64, inputKeys.get("longitude").getDataType());
    Assert.assertEquals(Optional.empty(), inputKeys.get("longitude").getListElementType());
    Assert.assertEquals(ValueType.FLOAT64, inputKeys.get("latitude").getDataType());
    Assert.assertEquals(Optional.empty(), inputKeys.get("latitude").getListElementType());
  }

  private void checkFeatureValues(Map<String, NameAndType> featureValues) {
    Assert.assertEquals(2, featureValues.size());
    Assert.assertEquals(
        ValueType.ARRAY, featureValues.get("average_rain.average_temperate_6hrs").getDataType());
    Assert.assertEquals(
        ValueType.FLOAT64,
        featureValues.get("average_rain.average_temperate_6hrs").getListElementType().get());

    Assert.assertEquals(
        ValueType.BOOLEAN,
        featureValues.get("average_rain.precipitation_higher_than_average").getDataType());
    Assert.assertEquals(
        Optional.empty(),
        featureValues.get("average_rain.precipitation_higher_than_average").getListElementType());
  }
}
