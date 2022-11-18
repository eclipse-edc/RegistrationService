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

package org.eclipse.edc.registration.api;


import org.eclipse.edc.api.transformer.DtoTransformerRegistry;
import org.eclipse.edc.registration.authority.model.Participant;
import org.eclipse.edc.registration.model.ParticipantDto;
import org.eclipse.edc.registration.store.spi.ParticipantStore;
import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.result.Result;
import org.eclipse.edc.spi.telemetry.Telemetry;
import org.eclipse.edc.web.spi.exception.ObjectNotFoundException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.eclipse.edc.registration.authority.model.ParticipantStatus.ONBOARDING_INITIATED;

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
     * Lists all dataspace participants.
     *
     * @return list of dataspace participants as DTOs.
     */
    public List<ParticipantDto> listParticipants() {
        monitor.info("List all participants of the dataspace.");

        return participantStore.listParticipants().stream()
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
                .id(UUID.randomUUID().toString())
                .did(did)
                .status(ONBOARDING_INITIATED)
                .traceContext(telemetry.getCurrentTraceContext())
                .build();

        participantStore.save(participant);
    }
}
