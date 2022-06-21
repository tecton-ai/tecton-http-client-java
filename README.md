# Java Client Library for Tecton Online Feature Store

A simple Java client for the Feature Server HTTP API that helps customers integrate with Tecton easily.

## Installation

Any Java client application using build frameworks such as Maven or Gradle can simply declare dependency on the current or appropriate version of `tecton-http-client-java` artifact to ensure all required JAR dependencies are resolved and available at runtime. The JAR can also be downloaded from the repository and manually imported into the project.

### Installation for Maven Project

Add the following dependency to `pom.xml`

```xml
<dependency>
<groupId>com.tecton.http-client</groupId>
<artifactId>tecton-http-client</artifactId>
</dependency>
```

###Installation for Gradlle Project
Add the following to `build.gradle`

```java
repositories {
      mavenCentral()
  }
  dependencies {
      compile 'com.tecton.http-client:tecton-http-client:<VERSION>'
  }
```

### Manual Installation

Download the Client Library as a zip or tar file from Maven Central, extract and place them directly in the application `CLASSPATH`

### Build the Java Client 

There is a `build.gradle` file provided to build the JAR from source and the client library can be built by running `./gradlew clean build` from the repository root. By default, the gradle build will generate `tecton-http-client-java-<VERSION>-SNAPSHOT.jar` in `build/libs`.

### Getting Started
//TODO

### API Reference
To learn more about the supported HTTP API endpoints, refer to the [Feature Server API Documentation](https://docs.tecton.ai/rest-swagger/docs.html)

### Contributions