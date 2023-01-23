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

package org.eclipse.edc.registration.service;


import org.eclipse.edc.registration.spi.model.Participant;
import org.eclipse.edc.registration.spi.registration.RegistrationService;
import org.eclipse.edc.registration.store.spi.ParticipantStore;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.telemetry.Telemetry;
import org.eclipse.edc.transaction.spi.TransactionContext;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

import static java.lang.String.format;
import static org.eclipse.edc.registration.spi.model.ParticipantStatus.ONBOARDING_INITIATED;

public class RegistrationServiceImpl implements RegistrationService {

    private final Monitor monitor;
    private final ParticipantStore participantStore;
    private final Telemetry telemetry;
    private final TransactionContext transactionContext;

    public RegistrationServiceImpl(Monitor monitor, ParticipantStore participantStore, Telemetry telemetry, TransactionContext transactionContext) {
        this.monitor = monitor;
        this.participantStore = participantStore;
        this.telemetry = telemetry;
        this.transactionContext = transactionContext;
    }

    @Nullable
    public Participant findByDid(String did) {
        monitor.info(format("Find a participant by DID %s", did));
        return participantStore.findByDid(did);
    }

    public List<Participant> listParticipants() {
        monitor.info("List all participants of the dataspace.");
        return transactionContext.execute(participantStore::listParticipants);
    }

    public void addParticipant(String did) {
        monitor.info("Adding a participant in the dataspace.");

        var participant = Participant.Builder.newInstance()
                .id(UUID.randomUUID().toString())
                .did(did)
                .status(ONBOARDING_INITIATED)
                .traceContext(telemetry.getCurrentTraceContext())
                .build();

        transactionContext.execute(() -> participantStore.save(participant));
    }
}
