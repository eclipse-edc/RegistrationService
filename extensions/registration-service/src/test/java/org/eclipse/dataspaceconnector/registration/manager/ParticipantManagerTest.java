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

package org.eclipse.dataspaceconnector.registration.manager;

import org.eclipse.dataspaceconnector.registration.authority.model.Participant;
import org.eclipse.dataspaceconnector.registration.authority.model.ParticipantStatus;
import org.eclipse.dataspaceconnector.registration.authority.spi.ParticipantVerifier;
import org.eclipse.dataspaceconnector.registration.store.spi.ParticipantStore;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.result.Result;
import org.eclipse.dataspaceconnector.spi.system.ExecutorInstrumentation;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.dataspaceconnector.registration.TestUtils.createParticipant;
import static org.eclipse.dataspaceconnector.registration.authority.model.ParticipantStatus.AUTHORIZED;
import static org.eclipse.dataspaceconnector.registration.authority.model.ParticipantStatus.AUTHORIZING;
import static org.eclipse.dataspaceconnector.registration.authority.model.ParticipantStatus.DENIED;
import static org.eclipse.dataspaceconnector.registration.authority.model.ParticipantStatus.ONBOARDING_INITIATED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ParticipantManagerTest {

    Monitor monitor = mock(Monitor.class);
    ParticipantStore participantStore = mock(ParticipantStore.class);
    ParticipantVerifier credentialsVerifier = mock(ParticipantVerifier.class);
    ParticipantManager service = new ParticipantManager(monitor, participantStore, credentialsVerifier, ExecutorInstrumentation.noop());
    Participant.Builder participantBuilder = createParticipant();
    ArgumentCaptor<Participant> captor = ArgumentCaptor.forClass(Participant.class);

    @Test
    void advancesStateFromOnboardingInitiatedToAuthorizing() throws Exception {
        advancesState(ONBOARDING_INITIATED, AUTHORIZING);
    }

    @Test
    void advancesStateFromAuthorizingToAuthorized() throws Exception {
        when(credentialsVerifier.verifyCredentials(any())).thenReturn(Result.success());
        advancesState(AUTHORIZING, AUTHORIZED);
    }

    @Test
    void advancesStateFromAuthorizingToDenied() throws Exception {
        when(credentialsVerifier.verifyCredentials(any())).thenReturn(Result.failure("foo"));
        advancesState(AUTHORIZING, DENIED);
    }

    @SuppressWarnings("unchecked")
    private void advancesState(ParticipantStatus startState, ParticipantStatus endState) throws Exception {
        var participant = participantBuilder.status(startState).build();
        when(participantStore.listParticipantsWithStatus(eq(startState)))
                .thenReturn(List.of(participant), List.of());
        var latch = new CountDownLatch(1);
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(participantStore).save(any());

        service.start();
        assertThat(latch.await(10, SECONDS)).isTrue();

        verify(participantStore).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(endState);
        assertThat(captor.getValue())
                .usingRecursiveComparison()
                .ignoringFieldsOfTypes(ParticipantStatus.class)
                .isEqualTo(participant);

        service.stop();
    }
}