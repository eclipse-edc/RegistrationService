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
import org.eclipse.edc.registration.store.spi.ParticipantStore;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.telemetry.Telemetry;
import org.eclipse.edc.transaction.spi.NoopTransactionContext;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.edc.registration.ParticipantUtils.createParticipant;
import static org.eclipse.edc.registration.spi.model.ParticipantStatus.ONBOARDING_INITIATED;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RegistrationServiceImplTest {

    private static final String DID = "some.test/url";

    private final Monitor monitor = mock(Monitor.class);
    private final ParticipantStore participantStore = mock(ParticipantStore.class);
    private final Telemetry telemetryMock = mock(Telemetry.class);

    private RegistrationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new RegistrationServiceImpl(monitor, participantStore, telemetryMock, new NoopTransactionContext());
    }

    @Test
    void listParticipants_empty() {
        when(participantStore.listParticipants()).thenReturn(List.of());

        assertThat(service.listParticipants()).isEmpty();

        verify(participantStore).listParticipants();
    }

    @Test
    void listParticipants() {
        var participant = createParticipant().build();
        when(participantStore.listParticipants()).thenReturn(List.of(participant));

        var result = service.listParticipants();

        assertThat(result).containsExactly(participant);
    }

    @Test
    void addParticipant() {
        var traceContext = getTraceContext();
        when(telemetryMock.getCurrentTraceContext()).thenReturn(traceContext);

        service.addParticipant(DID);

        var captor = ArgumentCaptor.forClass(Participant.class);
        verify(participantStore).save(captor.capture());
        var participant = captor.getValue();
        assertThat(captor.getValue())
                .usingRecursiveComparison()
                .isEqualTo(Participant.Builder.newInstance()
                        .did(DID)
                        .id(participant.getId())
                        .status(ONBOARDING_INITIATED)
                        .stateTimestamp(participant.getStateTimestamp())
                        .createdAt(participant.getCreatedAt())
                        .updatedAt(participant.getUpdatedAt())
                        .traceContext(traceContext)
                        .build());
    }

    @Test
    void findByDid() {
        var participant = createParticipant().build();
        when(participantStore.findByDid(participant.getDid()))
                .thenReturn(participant);

        assertThat(service.findByDid(participant.getDid())).isEqualTo(participant);
    }

    @Test
    void findByDid_notFound() {
        var participant = createParticipant().build();
        when(participantStore.findByDid(participant.getDid())).thenReturn(null);

        assertThat(service.findByDid(participant.getDid())).isNull();
    }

    @NotNull
    private Map<String, String> getTraceContext() {
        return Map.of("key1", "value1", "key2", "value2");
    }

}
