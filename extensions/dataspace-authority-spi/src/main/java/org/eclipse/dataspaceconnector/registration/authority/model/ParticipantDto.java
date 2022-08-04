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

import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Schema(description = "Dataspace Participant")
public class ParticipantDto {
    private String did;
    private String name;
    private String url;
    private List<String> supportedProtocols = new ArrayList<>();
    private ParticipantStatusDto status;

    private ParticipantDto() {
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

    public ParticipantStatusDto getStatus() {
        return status;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {
        private final ParticipantDto participantDto;

        private Builder() {
            participantDto = new ParticipantDto();
        }

        public static Builder newInstance() {
            return new Builder();
        }

        @Deprecated
        public Builder supportedProtocol(String supportedProtocol) {
            participantDto.supportedProtocols.add(supportedProtocol);
            return this;
        }

        @Deprecated
        public Builder supportedProtocols(List<String> supportedProtocols) {
            participantDto.supportedProtocols.addAll(supportedProtocols);
            return this;
        }

        @Deprecated
        public Builder url(String url) {
            participantDto.url = url;
            return this;
        }

        public Builder did(String did) {
            participantDto.did = did;
            return this;
        }

        @Deprecated
        public Builder name(String name) {
            participantDto.name = name;
            return this;
        }

        public Builder status(ParticipantStatusDto status) {
            participantDto.status = status;
            return this;
        }

        public ParticipantDto build() {
            Objects.requireNonNull(participantDto.did, "did");
            Objects.requireNonNull(participantDto.status, "status");
            return participantDto;
        }
    }

}
