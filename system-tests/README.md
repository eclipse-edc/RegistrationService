## System tests

These tests deploy the application in a docker environment, and use the generated client to interact with it.

### Running test locally

Build the application launchers:

```bash
./gradlew -DuseFsVault="true" shadowJar
```

Run the application using Docker compose:

```bash
docker-compose -f system-tests/docker-compose.yml up --build
```

Run tests:

```bash
INTEGRATION_TEST=true ./gradlew :system-tests:test
```