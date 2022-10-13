/*
 *  Copyright (c) 2022 Microsoft Corporation
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Microsoft Corporation - initial API and implementation
 *
 */

package org.eclipse.dataspaceconnector.registration.authority.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import org.eclipse.dataspaceconnector.spi.entity.StatefulEntity;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableMap;
import static org.eclipse.dataspaceconnector.registration.authority.model.ParticipantStatus.AUTHORIZED;
import static org.eclipse.dataspaceconnector.registration.authority.model.ParticipantStatus.AUTHORIZING;
import static org.eclipse.dataspaceconnector.registration.authority.model.ParticipantStatus.DENIED;
import static org.eclipse.dataspaceconnector.registration.authority.model.ParticipantStatus.FAILED;
import static org.eclipse.dataspaceconnector.registration.authority.model.ParticipantStatus.ONBOARDED;
import static org.eclipse.dataspaceconnector.registration.authority.model.ParticipantStatus.ONBOARDING_INITIATED;

/**
 * Dataspace participant.
 */
@JsonDeserialize(builder = Participant.Builder.class)
public class Participant extends StatefulEntity<Participant> {

    private String did;

    private Participant() {
    }

    public String getDid() {
        return did;
    }

    public ParticipantStatus getStatus() {
        return ParticipantStatus.from(state);
    }

    @Override
    public Participant copy() {
        var builder = Builder.newInstance()
                .did(did);

        return copy(builder);
    }

    public void transitionAuthorizing() {
        transition(AUTHORIZING, ONBOARDING_INITIATED);
    }

    public void transitionAuthorized() {
        transition(AUTHORIZED, AUTHORIZING);
    }

    public void transitionDenied() {
        transition(DENIED, AUTHORIZING);
    }

    public void transitionOnboarded() {
        transition(ONBOARDED, AUTHORIZED);
    }

    public void transitionFailed() {
        transition(FAILED, AUTHORIZING, AUTHORIZED);
    }

    /**
     * Transition to a given end state from an allowed number of previous states.
     *
     * @param end    The desired state.
     * @param starts The allowed previous states.
     */
    private void transition(ParticipantStatus end, ParticipantStatus... starts) {
        if (Arrays.stream(starts).noneMatch(s -> s.code() == state)) {
            throw new IllegalStateException(format("Cannot transition from state %s to %s", ParticipantStatus.from(state), end));
        }
        transitionTo(end.code());
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder extends StatefulEntity.Builder<Participant, Builder> {

        private Builder(Participant participant) {
            super(participant);
        }

        @JsonCreator
        public static Builder newInstance() {
            return new Builder(new Participant());
        }

        public Builder did(String did) {
            entity.did = did;
            return this;
        }

        public Builder status(ParticipantStatus status) {
            entity.state = status.code();
            return this;
        }

        @Override
        public Builder traceContext(Map<String, String> traceContext) {
            entity.traceContext = unmodifiableMap(traceContext);
            return this;
        }

        @Override
        public Participant build() {
            Objects.requireNonNull(entity.did, "did");
            return super.build();
        }


        @Override
        public Builder self() {
            return this;
        }
    }
}
