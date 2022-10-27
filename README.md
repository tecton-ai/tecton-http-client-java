# Java Client Library for Tecton Online Feature Store

A simple Java client for the Feature Server HTTP API that helps customers integrate with Tecton easily.

## Documentation

* [Fetching Online Features](https://docs.tecton.ai/latest/examples/fetch-real-time-features.html)
* [FeatureServer API Reference](https://docs.tecton.ai/rest-swagger/docs.html)
* [Tecton Java Client API Reference](https://www.javadoc.io/doc/ai.tecton/java-client/latest/index.html)
* [Tecton Java Client Example Code](https://github.com/tecton-ai/TectonClientDemo/tree/main/src/main/java)

## Troubleshooting

If you have any questions or need help, please [open an Issue](https://github.com/tecton-ai/tecton-http-client-java/issues) or reach out to us in Slack!

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

In the demo client [repository](https://github.com/tecton-ai/TectonClientDemo) update the `build.gradle` file with the jar that you generate from this repo using `./gradlew clean build`.

Change the dependencies target to this and point the files attribute to your java client jar:

```
dependencies {
    implementation files('libs/java-client-0.1.0-SNAPSHOT.jar')
    implementation 'com.google.code.gson:gson:2.2.4'
    implementation group: 'org.apache.commons', name: 'commons-lang3', version: '3.12.0'

}
```

Update `tecton.properties` with your cluster url and run the Demo file to query the feature services needed.

## License

The project is licensed under [Apache License 2.0](https://github.com/tecton-ai/tecton-http-client-java/blob/main/LICENSE.md)
