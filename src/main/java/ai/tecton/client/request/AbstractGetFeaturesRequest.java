package ai.tecton.client.request;

import ai.tecton.client.exceptions.TectonClientException;
import ai.tecton.client.exceptions.TectonErrorMessage;
import ai.tecton.client.model.MetadataOption;
import ai.tecton.client.transport.TectonHttpClient.HttpMethod;
import java.util.*;

abstract class AbstractGetFeaturesRequest extends AbstractTectonRequest {

  private static final HttpMethod httpMethod = HttpMethod.POST;
  final Set<MetadataOption> metadataOptions;

  AbstractGetFeaturesRequest(
      String workspaceName,
      String featureServiceName,
      String endpoint,
      Set<MetadataOption> metadataOptions)
      throws TectonClientException {
    super(endpoint, httpMethod, workspaceName, featureServiceName);
    if (metadataOptions == null || metadataOptions.size() == 0) {
      this.metadataOptions = RequestConstants.DEFAULT_METADATA_OPTIONS;
    } else {
      this.metadataOptions = getMetadataOptions(metadataOptions);
    }
  }

  Set<MetadataOption> getMetadataOptions() {
    return this.metadataOptions;
  }

  static Set<MetadataOption> getMetadataOptions(Set<MetadataOption> metadataOptions) {
    Set<MetadataOption> finalMetadataOptionSet;
    if (metadataOptions.contains(MetadataOption.ALL)) {
      // Add everything except ALL and NONE from MetadataOption EnumSet
      finalMetadataOptionSet = RequestConstants.ALL_METADATA_OPTIONS;
    } else if (metadataOptions.contains(MetadataOption.NONE)) {
      finalMetadataOptionSet = RequestConstants.NONE_METADATA_OPTIONS;
    } else {
      finalMetadataOptionSet = metadataOptions;
    }
    finalMetadataOptionSet.addAll(
        RequestConstants.DEFAULT_METADATA_OPTIONS); // add default metadata options
    return finalMetadataOptionSet;
  }

  static void validateRequestParameters(GetFeaturesRequestData getFeaturesRequestData) {
    if (getFeaturesRequestData.isEmptyJoinKeyMap()
        && getFeaturesRequestData.isEmptyRequestContextMap()) {
      throw new TectonClientException(TectonErrorMessage.EMPTY_REQUEST_MAPS);
    }
  }
}
