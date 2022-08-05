# State Machine

## Decision

The participant lifecycle is managed in the registration service using a state machine. The state machine is operated in such a way that domain objects are loaded from storage, processed and then put back into storage to make the registration service runtime stateless.

The [EDC state machine module](https://github.com/eclipse-dataspaceconnector/DataSpaceConnector/blob/70271b5b3427c9a26198fa8d43a08519be4ffba6/common/state-machine-lib/src/main/java/org/eclipse/dataspaceconnector/common/statemachine/StateMachine.java) is used to implement the state machine.

## Rationale

The registration service enrollment endpoint onboards new participant asynchronously and during this processing a participant goes through different states.

In simple scenarios, enrollment could be fast and fully automated. However, in advanced scenarios, enrollment policies could require interactions with external systems, and even manual processes. Therefore, it is implemented asynchronously.
