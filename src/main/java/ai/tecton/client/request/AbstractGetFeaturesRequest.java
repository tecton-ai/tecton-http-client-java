package ai.tecton.client.request;

import ai.tecton.client.exceptions.TectonClientException;
import ai.tecton.client.exceptions.TectonErrorMessage;
import ai.tecton.client.model.MetadataOption;
import ai.tecton.client.transport.TectonHttpClient.HttpMethod;
import java.util.*;

abstract class AbstractGetFeaturesRequest extends AbstractTectonRequest {

  private static final HttpMethod httpMethod = HttpMethod.POST;

  // Always include name and data_type in metadata options
  static final Set<MetadataOption> defaultMetadataOptions =
      EnumSet.of(MetadataOption.NAME, MetadataOption.DATA_TYPE);
  final Set<MetadataOption> metadataOptions;

  AbstractGetFeaturesRequest(
      String workspaceName,
      String featureServiceName,
      String endpoint,
      MetadataOption... metadataOptions)
      throws TectonClientException {
    super(endpoint, httpMethod, workspaceName, featureServiceName);
    if (metadataOptions == null || metadataOptions.length == 0) {
      this.metadataOptions = defaultMetadataOptions;
    } else {
      this.metadataOptions = getMetadataOptions(metadataOptions);
    }
  }

  Set<MetadataOption> getMetadataOptions() {
    return this.metadataOptions;
  }

  static Set<MetadataOption> getMetadataOptions(MetadataOption... metadataOptions) {
    List<MetadataOption> metadataOptionList = Arrays.asList(metadataOptions);
    Set<MetadataOption> finalMetadataOptionSet;
    if (metadataOptionList.contains(MetadataOption.ALL)) {
      // Add everything except ALL and NONE from MetadataOption EnumSet
      finalMetadataOptionSet =
          EnumSet.complementOf(EnumSet.of(MetadataOption.ALL, MetadataOption.NONE));
    } else if (metadataOptionList.contains(MetadataOption.NONE)) {
      finalMetadataOptionSet = EnumSet.noneOf(MetadataOption.class);
    } else {
      finalMetadataOptionSet = EnumSet.copyOf(metadataOptionList);
    }
    finalMetadataOptionSet.addAll(defaultMetadataOptions); // default metadata options
    return finalMetadataOptionSet;
  }

  static void validateRequestParameters(GetFeaturesRequestData getFeaturesRequestData) {
    if (getFeaturesRequestData.isEmptyJoinKeyMap()
        && getFeaturesRequestData.isEmptyRequestContextMap()) {
      throw new TectonClientException(TectonErrorMessage.EMPTY_REQUEST_MAPS);
    }
  }
}
