# Command-Line client

## Decision

A command-line client tool is provided to access the Registration Service.

The [picocli](https://picocli.info) framework is used to build the command-line tool.

## Rationale

It is foreseen that a dataspace participant can be deployed without having an EDC connector instance deployed yet. Nevertheless, signing an authenticated request to the Registration Service (with a did:web JSON Web Token, as planned for later) requires access to the private key, and executing custom code.

A command-line tool can be run by an administrator with access to the identity private key.

Picocli is a popular, well-maintained and lightweight tool. It produced elegant console output, and provides good developer productivity.
