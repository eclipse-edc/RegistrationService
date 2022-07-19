# HTTP Ports

## Decision

The EDC `default` web context is deployed on HTTP port `8181`. This context contains the health endpoint at `http://localhost:8181/api/check/health`.

The Registration Service REST Controller is deployed in a additional EDC web context named `authority`.

The port mapping and REST URL path for this context must be specified in deployment.

For example in Docker Compose:

```
    environment:
      WEB_HTTP_AUTHORITY_PORT: 8182
      WEB_HTTP_AUTHORITY_PATH: /authority
```

This makes the List Participants endpoint available at `http://localhost:8182/authority/registry/participants`.

## Rationale

DID-based JWS authentication will be used for the Registration Service controller, using a JAX-RS filter.

However, for docker health check (used in `docker-compose up --wait` in CI to wait until containers have successfully started), we use `curl` to access the health endpoint, which is deployed in the EDC default context. Therefore, we do not want to apply our authentication filter to the `default` context, and need to introduce an additional context for the API controller.

It is also good practice not to expose health and management endpoints to public access. Deploying them on a different ports allow deployments to expose their port on internal routes only.
