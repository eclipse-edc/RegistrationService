# Membership Verifiable Credential

## Decision

During participant onboarding, the Registration Service issues a dataspace membership verifiable credential (VC) and store it in the participant's Identity Hub.

The participant's DID Document must contain a Service Endpoint of type `IdentityHub`, indicating the URL at which the Identity Hub API is available. Access to Identity Hub is currently not authenticated.

After a participant has been authorised, the Registration Service creates a verifiable credential in JWT format (signed as JWS) and pushes it to the participant's Identity Hub endpoint. After that, the participant is fully onboarded.

The Verifiable Credential JWT contains the following claims. The JWT contains a combination of RFC 7519 [Registered Claim Names](https://datatracker.ietf.org/doc/html/rfc7519#section-4.1) and [Private Claim Names](https://datatracker.ietf.org/doc/html/rfc7519#section-4.3).

| Claim              | Value                                                        |
| ------------------ | ------------------------------------------------------------ |
| Issuer (`iss`)     | The Dataspace [did:web](https://w3c-ccg.github.io/did-method-web/) identifier (example: `did:web:example.com`). The Issuer Claim must resolve to a DID Document containing the public key of the key pair used to sign the JWS. This allows the server to verify the token signature against the source's public key. |
| Subject (`sub`)    | The Participant [did:web](https://w3c-ccg.github.io/did-method-web/) identifier. |
| Issue At (`iat`)   | The credential creation date.                                |
| JWT ID (`jti`)     | A random UUID.                                               |
| `vc` Private Claim | A JSON payload containing the verifiable credential claim.   |

The `vc` claim payload has the following format:

```json
{
	"id": "<a random GUID>",
	"credentialSubject": {
		"memberOfDataspace": "<Dataspace did:web identifier>"
	}
}
```

## Rationale

The membership credential can be used by participants' EDC policy engine to restrict assets to dataspace members.
