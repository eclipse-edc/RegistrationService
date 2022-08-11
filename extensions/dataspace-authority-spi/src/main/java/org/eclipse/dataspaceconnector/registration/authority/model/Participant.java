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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static java.lang.String.format;
import static org.eclipse.dataspaceconnector.registration.authority.model.ParticipantStatus.AUTHORIZED;
import static org.eclipse.dataspaceconnector.registration.authority.model.ParticipantStatus.AUTHORIZING;
import static org.eclipse.dataspaceconnector.registration.authority.model.ParticipantStatus.DENIED;
import static org.eclipse.dataspaceconnector.registration.authority.model.ParticipantStatus.FAILED;
import static org.eclipse.dataspaceconnector.registration.authority.model.ParticipantStatus.ONBOARDED;
import static org.eclipse.dataspaceconnector.registration.authority.model.ParticipantStatus.ONBOARDING_INITIATED;

/**
 * Dataspace participant.
 * <p>
 * In a future version, the {@link #name}, {@link #url} and {@link #supportedProtocols} fields will be removed,
 * as the {@link #did} provides sufficient information to identify the participant, and additional
 * information can be retrieved from the participant's DID document.
 */
@JsonDeserialize(builder = Participant.Builder.class)
public class Participant {

    private String did;
    private String name;
    private String url;
    private List<String> supportedProtocols = new ArrayList<>();
    private ParticipantStatus status = ONBOARDING_INITIATED;

    private Participant() {
    }

    @Deprecated
    public String getName() {
        return name;
    }

    @Deprecated
    public String getUrl() {
        return url;
    }

    public String getDid() {
        return did;
    }

    @Deprecated
    public List<String> getSupportedProtocols() {
        return Collections.unmodifiableList(supportedProtocols);
    }

    public ParticipantStatus getStatus() {
        return status;
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
        transition(FAILED, AUTHORIZED);
    }

    /**
     * Transition to a given end state from an allowed number of previous states.
     *
     * @param end    The desired state.
     * @param starts The allowed previous states.
     */
    private void transition(ParticipantStatus end, ParticipantStatus... starts) {
        if (Arrays.stream(starts).noneMatch(s -> s == status)) {
            throw new IllegalStateException(format("Cannot transition from state %s to %s", status, end));
        }
        status = end;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {
        private final Participant participant;

        private Builder() {
            participant = new Participant();
        }

        public static Builder newInstance() {
            return new Builder();
        }

        @Deprecated
        public Builder supportedProtocol(String supportedProtocol) {
            participant.supportedProtocols.add(supportedProtocol);
            return this;
        }

        @Deprecated
        public Builder supportedProtocols(List<String> supportedProtocols) {
            participant.supportedProtocols.addAll(supportedProtocols);
            return this;
        }

        @Deprecated
        public Builder url(String url) {
            participant.url = url;
            return this;
        }

        public Builder did(String did) {
            participant.did = did;
            return this;
        }

        @Deprecated
        public Builder name(String name) {
            participant.name = name;
            return this;
        }

        public Builder status(ParticipantStatus status) {
            participant.status = status;
            return this;
        }

        public Participant build() {
            Objects.requireNonNull(participant.did, "did");
            Objects.requireNonNull(participant.status, "status");
            return participant;
        }
    }
}
