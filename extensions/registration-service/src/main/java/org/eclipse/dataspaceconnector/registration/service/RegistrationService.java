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

package org.eclipse.dataspaceconnector.registration.service;

import org.eclipse.dataspaceconnector.api.transformer.DtoTransformerRegistry;
import org.eclipse.dataspaceconnector.registration.authority.model.Participant;
import org.eclipse.dataspaceconnector.registration.model.ParticipantDto;
import org.eclipse.dataspaceconnector.registration.store.spi.ParticipantStore;
import org.eclipse.dataspaceconnector.spi.EdcException;
import org.eclipse.dataspaceconnector.spi.exception.ObjectNotFoundException;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.result.Result;
import org.eclipse.dataspaceconnector.spi.telemetry.Telemetry;

import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.eclipse.dataspaceconnector.registration.authority.model.ParticipantStatus.ONBOARDED;
import static org.eclipse.dataspaceconnector.registration.authority.model.ParticipantStatus.ONBOARDING_INITIATED;

/**
 * Registration service for dataspace participants.
 */
public class RegistrationService {

    private final Monitor monitor;
    private final ParticipantStore participantStore;
    private final DtoTransformerRegistry transformerRegistry;
    private final Telemetry telemetry;

    public RegistrationService(Monitor monitor, ParticipantStore participantStore, DtoTransformerRegistry transformerRegistry, Telemetry telemetry) {
        this.monitor = monitor;
        this.participantStore = participantStore;
        this.transformerRegistry = transformerRegistry;
        this.telemetry = telemetry;
    }

    /**
     * Find a participant by DID.
     *
     * @param did DID of participant.
     * @return Participant DTO.
     */
    public ParticipantDto findByDid(String did) {
        monitor.info(format("Find a participant by DID %s", did));

        var participant = participantStore.findByDid(did);
        if (participant == null) {
            throw new ObjectNotFoundException(Participant.class, did);
        }
        var result = transformerRegistry.transform(participant, ParticipantDto.class);
        if (result.failed()) {
            throw new EdcException(result.getFailureDetail());
        }
        return result.getContent();
    }

    /**
     * Lists all dataspace participants in state {@ref #ONBOARDED}.
     *
     * @return list of dataspace participants as DTOs.
     */
    public List<ParticipantDto> listOnboardedParticipants() {
        monitor.info("List onboarded participants of the dataspace.");

        return participantStore
                .listParticipantsWithStatus(ONBOARDED)
                .stream()
                .map(participant -> transformerRegistry.transform(participant, ParticipantDto.class))
                .filter(Result::succeeded)
                .map(Result::getContent)
                .collect(Collectors.toList());
    }

    /**
     * Add a participant to a dataspace.
     *
     * @param did the DID of the dataspace participant to add.
     */
    public void addParticipant(String did) {
        monitor.info("Adding a participant in the dataspace.");

        var participant = Participant.Builder.newInstance()
                .did(did)
                .status(ONBOARDING_INITIATED)
                .traceContext(telemetry.getCurrentTraceContext())
                .build();

        participantStore.save(participant);
    }
}
