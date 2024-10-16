# Java Client Library for Tecton Online Feature Store

A simple Java client for the Feature Server HTTP API that helps customers integrate with Tecton easily.

## Documentation

* [Fetching Online Features](https://docs.tecton.ai/latest/examples/fetch-real-time-features.html)
* [FeatureServer API Reference](https://docs.tecton.ai/rest-swagger/docs.html)
* [Tecton Java Client API Reference](https://www.javadoc.io/doc/ai.tecton/java-client/latest/index.html)
* [Tecton Java Client Example Code](https://github.com/tecton-ai/TectonClientDemo/tree/main/src/main/java)

## Troubleshooting

If you have any questions or need help,
please [open an Issue](https://github.com/tecton-ai/tecton-http-client-java/issues) or reach out to us in Slack!

## Contributing

The Tecton Java client is open source and we welcome any contributions from our Tecton community.

### Prerequisites

* Java 8 or higher
* Gradle
* [Google Java Format](https://github.com/google/google-java-format) formatter (can also use as a plugin in your IDE)

### Build the Project

The `tecton-http-client-java` project can be built using Gradle as follows:

`./gradlew clean build`

## Basic end to end testing

In the demo client [repository](https://github.com/tecton-ai/TectonClientDemo) update the `build.gradle` file with the
jar that you generate from this repo using `./gradlew clean build`.

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
}
```

Update `tecton.properties` with your cluster url and run the Demo file to query the feature services needed.

## Before Opening a PR

* Please run pre-commit on your staged files to ensure that the changes are correctly formatted.
* Please run `./gradlew clean build` to ensure that your changes pass the build
* Please add unit tests if possible to test the new code changes

## License

The project is licensed
under [Apache License 2.0](https://github.com/tecton-ai/tecton-http-client-java/blob/main/LICENSE.md)
