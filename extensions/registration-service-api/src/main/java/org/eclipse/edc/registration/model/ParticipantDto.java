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

package org.eclipse.edc.registration.model;

import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

@Schema(description = "Dataspace Participant DTO")
public class ParticipantDto {
    private String did;
    private ParticipantStatusDto status;

    private ParticipantDto() {
    }

    public String getDid() {
        return did;
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

        public Builder did(String did) {
            participantDto.did = did;
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
