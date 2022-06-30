# Command-line client

The client is a Java JAR that provides access to a registration service via REST.

## Running the client

To run the command line client, and list available options and commands:

```bash
cd RegistrationService
./gradlew build
java -jar client-cli/build/libs/registration-service-cli.jar --help
```

For example, to list dataspace participants:

```
java -jar client-cli/build/libs/registration-service-cli.jar \
  -s=http://localhost:8182/authority \
  participants list
```

The client can also be run from a local Maven repository:

```
cd RegistrationService
./gradlew publishToMavenLocal
```

```
cd OtherDirectory
mvn dependency:copy -Dartifact=org.eclipse.dataspaceconnector.registrationservice:registration-service-cli:1.0.0-SNAPSHOT:jar:all -DoutputDirectory=.
java -jar registration-service-cli-1.0.0-SNAPSHOT-all.jar --help
```

