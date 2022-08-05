## System tests

These tests deploy the application in a docker environment, and use the generated client to interact with it.

### Running test locally

Build the application launchers:

```
./gradlew -DuseFsVault="true" shadowJar
```

Run the application using Docker compose:

```
docker-compose up --build
```

Run tests:
```
INTEGRATION_TEST=true ./gradlew :system-tests:test
```
