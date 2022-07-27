# Registration Service API

Registration Service is a component of a Dataspace Authority. 
The Registration Service is responsible for the management of the participants in the Dataspace.

The Registration Service exposes an API that offers the following operations:
- Add a participant to the Dataspace
- List participants in the Dataspace

The Registration Service is written in Java and uses the runtime framework and modules from [EDC](https://github.com/eclipse-dataspaceconnector/DataSpaceConnector).

## OpenApi 

The Registration Service API definition is available in an [OpenApi yaml file](../../../../resources/openapi/yaml/registration-service.yaml). 

## API Rest client

The Registration Service API can be called using the [rest-client](../../../../rest-client) generated from OpenApi definition.

```java
ApiClient apiClient = ApiClientFactory.createApiClient(API_URL);
RegistryApi api = new RegistryApi(apiClient);
```

Currently the Registration Service client is not published to any public artifactory so the rest client can be used in local development after publishing the 
Registration Service artifacts locally

```
./gradlew publishToMavenLocal
```

or by regenerating the client from [OpenApi file](../../../../resources/openapi/yaml/registration-service.yaml).

## Participants store

The Registration Service stores participants in an in-memory hashmap store. 

## API Operations

### Add participant

Calling POST method to add participant triggers an enrollment process of a new Dataspace member. The new participant is saved to the store for further 
processing. 

### List participants

Calling GET method to list participants returns a list of all participants that are saved in the store.


