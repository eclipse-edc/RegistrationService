# Service authentication

## Decision

Access to the Registration Service API is authenticated.

Callers must provide a Bearer token containing a JSON Web Token ([JWT](https://datatracker.ietf.org/doc/html/rfc7519)) transmitted as a JSON Web Signature ([JWS](https://www.rfc-editor.org/rfc/rfc7515)) signed with an ECDSA P-256 Elliptic Curve key.

Tokens contain the following claims:

| Claim           | Value                                                        |
| --------------- | ------------------------------------------------------------ |
| Issuer          | The participant [did:web](https://w3c-ccg.github.io/did-method-web/) identifier (example: `did:web:example.com`). The Issuer Claim must resolve to a DID Document containing the public key of the key pair used to sign the JWS. This allows the server to verify the token signature against the source's public key. |
| Subject         | The fixed string `verifiable-credential`.                    |
| Audience        | The Registration Service API base endpoint (example: `http://dataspace.example.com:8182/authority`). This allows the server to verify the intended [audience](https://datatracker.ietf.org/doc/html/rfc7519#section-4.1.3). |
| JWT ID          | A random UUID.                                               |
| Expiration Time | A time set in the near future.                               |

Authentication is implemented as follows:

- On the server side, a `ContainerRequestFilter` intercepts incoming requests, retrieves the DID Document corresponding to the issuer, verifies token signature and claims, and inserts a new request header containing the Issuer claim. This allows controllers to use the claim for application logic (for example, to register a new participant by its DID).
  The filter leverages the services provided in the EDC `identity-did-core` and `identity-did-crypto` modules to verify tokens and to resolve and parse DID documents.
  The server must be configured with a `jwt.audience` setting (e.g. as the `JWT_AUDIENCE` environment variable) containing the expected audience, i.e. the URL used by the client to access the API.
- On the CLI client side, a Request Interceptor modifies outgoing requests, to add the required JWS header. This requires the CLI to have access to a file containing the private key, and to be configured with the correct DID.

For local testing with Docker Compose, an `nginx` container is deployed to serve a DID Document. The `EDC_IAM_DID_WEB_USE_HTTPS`variable is used to use `http://` instead of the `https://` scheme for resolving DID URLs, to avoid having to set up SSL.

## Rationale

Authentication ensures that only actors who can assert their control of a did:web identifier can register that identifier as a dataspace participant. Authentication and retrieval of DID document will also serve as the basic block for retrieving verifiable credentials and building an authorization system.

The choice of authentication scheme and key type is driven by the schemes [currently supported in EDC](https://github.com/eclipse-dataspaceconnector/DataSpaceConnector/blob/89ffb983dbdebce02e1447d2f2f5f65843f46041/docs/developer/decision-records/2022-06-19-json-web-token/README.md), and can be extended as needed in the future.
