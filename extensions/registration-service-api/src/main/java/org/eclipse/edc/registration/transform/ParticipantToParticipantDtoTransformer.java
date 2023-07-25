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

package org.eclipse.edc.registration.transform;

import org.eclipse.edc.registration.model.ParticipantDto;
import org.eclipse.edc.registration.model.ParticipantStatusDto;
import org.eclipse.edc.registration.spi.model.Participant;
import org.eclipse.edc.registration.spi.model.ParticipantStatus;
import org.eclipse.edc.transform.spi.TransformerContext;
import org.eclipse.edc.transform.spi.TypeTransformer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ParticipantToParticipantDtoTransformer implements TypeTransformer<Participant, ParticipantDto> {

    @Override
    public Class<Participant> getInputType() {
        return Participant.class;
    }

    @Override
    public Class<ParticipantDto> getOutputType() {
        return ParticipantDto.class;
    }

    @Override
    public @Nullable ParticipantDto transform(@Nullable Participant participant, @NotNull TransformerContext context) {
        return ParticipantDto.Builder.newInstance()
                .did(participant.getDid())
                .status(mapToDtoStatus(participant.getStatus()))
                .build();
    }

    /**
     * Map domain model ParticipantStatus to DTO. This mapping is done to prevent leak of internal status.
     * DTO only exposes {@link ParticipantStatusDto#ONBOARDING_IN_PROGRESS}, {@link ParticipantStatusDto#ONBOARDED}
     * and {@link ParticipantStatusDto#DENIED} statues.
     *
     * @param status {@link ParticipantStatus}
     * @return {@link ParticipantStatusDto}
     */
    private ParticipantStatusDto mapToDtoStatus(ParticipantStatus status) {
        return switch (status) {
            case ONBOARDING_INITIATED, AUTHORIZING, AUTHORIZED -> ParticipantStatusDto.ONBOARDING_IN_PROGRESS;
            case ONBOARDED -> ParticipantStatusDto.ONBOARDED;
            case DENIED, FAILED -> ParticipantStatusDto.DENIED;
        };
    }
}
