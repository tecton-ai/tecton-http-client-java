# Java Client Library for Tecton Online Feature Store

A simple Java client for the Feature Server HTTP API that helps customers integrate with Tecton easily.

## Documentation

* [Fetching Online Features](https://docs.tecton.ai/latest/examples/fetch-real-time-features.html)
* [FeatureServer API Reference](https://docs.tecton.ai/rest-swagger/docs.html)
* [Tecton Java Client API Reference](https://www.javadoc.io/doc/ai.tecton/java-client/latest/index.html)
* [Tecton Java Client Example Code](https://github.com/tecton-ai/TectonClientDemo/tree/main/src/main/java)

## Usage Example

### Gradle Dependencies

In your `build.gradle` file for your project, make sure you're using the central Maven repository:

```
repositories {
      mavenCentral()
      // ...
}
```

And then you can depend on the Tecton client library:

```
dependencies {
      implementation "ai.tecton:java-client:$client_version"
      // ...
}
```

### Client Usage

Necessary imports:

```java
import ai.tecton.client.TectonClient;
import ai.tecton.client.TectonClientOptions;
import ai.tecton.client.request.GetFeaturesBatchRequest;
import ai.tecton.client.request.GetFeaturesRequestData;
import ai.tecton.client.request.RequestConstants;
import ai.tecton.client.response.GetFeaturesBatchResponse;
import ai.tecton.client.response.GetFeaturesResponse;
import ai.tecton.client.model.ValueType;
```

You can instantiate a `TectonClient` object for querying a particular Tecton cluster as a particular
service account:

```java
String apiKey = "apikey1";
String tectonUrl = "mycluster.tecton.ai";

TectonClient tectonClient = new TectonClient(tectonUrl, apiKey);
```

You can then make a request for a single (composite) join key:

```java
String workspaceName = "prod";
String featureServiceName = "fraud_service";
String featureName = "user_transaction_counts.feature_name";

GetFeaturesRequestData getFeaturesRequestData =
     new GetFeaturesRequestData()
          .addJoinKey("user_id", "user_205125746682")
          .addJoinKey("merchant", "entertainment")
          .addRequestContext("amt", 500.00);
GetFeaturesRequest req =
     new GetFeaturesRequest(workspaceName, featureServiceName, getFeaturesRequestData);

GetFeaturesResponse resp = tectonClient.getFeatures(req);
Map<String, FeatureValue> featureValues = resp.getFeatureValuesAsMap();
// Could also use getFeaturesResponse.getFeatureValues() for a List<FeatureValue>.

FeatureValue sampleFeatureValue = featureValues.get(featureName);

// Will be "user_transaction_counts".
String fns = sampleFeatureValue.getFeatureNamespace();

// Will be "feature_name".
String fn = sampleFeatureValue.getFeatureName();

// Will be ValueType.INT64.
ValueType vt = sampleFeatureValue.getValueType();

// Will be the value of the feature.
Long v = sampleFeatureValue.int64value();

// Other methods are available for other value types, such as:
// stringValue(), booleanValue(), float64Value(), float64ArrayValue(),
// float32ArrayValue(), int64ArrayValue(), and stringArrayValue().
```

Or you can make a batch request for multiple join keys:

```java
List<GetFeaturesRequestData> requestDataList = new ArrayList<>();
requestDataList.add(getFeaturesRequestData);
GetFeaturesBatchRequest batchRequest = new GetFeaturesBatchRequest(workspaceName, featureServiceName, getFeaturesRequestDataList, RequestConstants.DEFAULT_METADATA_OPTIONS, 5);
GetFeaturesBatchResponse batchResponse = tectonClient.getFeaturesBatch(batchRequest);

// Same as resp above.
GetFeaturesResponse sampleResponse = batchResponse.getBatchResponseList().get(0);
```

## Troubleshooting

If you have any questions or need help,
please [open an Issue](https://github.com/tecton-ai/tecton-http-client-java/issues) or reach out to us in Slack!

## Contributing

The Tecton Java client is open source and we welcome any contributions from our Tecton community.

### Prerequisites

* Java 8 or higher
* Maven
* [Google Java Format](https://github.com/google/google-java-format) formatter (can also use as a plugin in your IDE)

### Build the Project

The `tecton-http-client-java` project can be built using Maven as follows:

`./mvnw clean package`

## Basic end to end testing

In the demo client [repository](https://github.com/tecton-ai/TectonClientDemo) update the `build.gradle` file with the
jar that you generate from this repo using `./mvnw clean package`.

1. Change the dependencies target to this and point the files attribute to your java client jar.
2. Add the okhttp3 dependency from `tecton-http-client-java` to `build.gradle` in the demo client repository. This is
   necessary because it is an API dependency, but JAR files don't include information about transitive dependencies.
   Published libraries on Maven don't have this issue.

```
dependencies {
    implementation files('libs/java-client-0.1.0-SNAPSHOT.jar')
    // OkHttp Client
    implementation group: 'com.squareup.okhttp3', name: 'okhttp', version: '4.10.0'
    // StringUtils and Validation checks
    implementation group: 'org.apache.commons', name: 'commons-lang3', version: '3.12.0'
    testImplementation 'junit:junit:4.13.2'

    // Other dependencies which you may need to add
    implementation 'com.google.code.gson:gson:2.2.4'
    implementation group: 'com.squareup.moshi', name: 'moshi', version: '1.13.0'
    implementation group: 'com.squareup.moshi', name: 'moshi-adapters', version: '1.13.0'
    implementation group: 'org.apache.commons', name: 'commons-collections4', version: '4.4'
}
```

Update `tecton.properties` with your cluster url and run the Demo file to query the feature services needed.

## Before Opening a PR

* Please run pre-commit on your staged files to ensure that the changes are correctly formatted.
* Please run `./mvnw clean package` to ensure that your changes pass the build
* Please add unit tests if possible to test the new code changes

## License

The project is licensed
under [Apache License 2.0](https://github.com/tecton-ai/tecton-http-client-java/blob/main/LICENSE.md)
