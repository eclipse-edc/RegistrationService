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
import org.eclipse.dataspaceconnector.registration.credential.VerifiableCredentialService;
import org.eclipse.dataspaceconnector.registration.store.spi.ParticipantStore;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.response.ResponseStatus;
import org.eclipse.dataspaceconnector.spi.response.StatusResult;
import org.eclipse.dataspaceconnector.spi.system.ExecutorInstrumentation;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.dataspaceconnector.registration.authority.TestUtils.createParticipant;
import static org.eclipse.dataspaceconnector.registration.authority.model.ParticipantStatus.AUTHORIZED;
import static org.eclipse.dataspaceconnector.registration.authority.model.ParticipantStatus.AUTHORIZING;
import static org.eclipse.dataspaceconnector.registration.authority.model.ParticipantStatus.DENIED;
import static org.eclipse.dataspaceconnector.registration.authority.model.ParticipantStatus.FAILED;
import static org.eclipse.dataspaceconnector.registration.authority.model.ParticipantStatus.ONBOARDED;
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
    ParticipantVerifier participantVerifier = mock(ParticipantVerifier.class);
    VerifiableCredentialService verifiableCredentialService = mock(VerifiableCredentialService.class);
    ParticipantManager service = new ParticipantManager(monitor, participantStore, participantVerifier, ExecutorInstrumentation.noop(), verifiableCredentialService);
    Participant.Builder participantBuilder = createParticipant();
    ArgumentCaptor<Participant> captor = ArgumentCaptor.forClass(Participant.class);

    @Test
    void advancesStateFromOnboardingInitiatedToAuthorizing() throws Exception {
        advancesState(ONBOARDING_INITIATED, AUTHORIZING);
    }

    @Test
    void advancesStateFromAuthorizingToAuthorized() throws Exception {
        when(participantVerifier.isOnboardingAllowed(any())).thenReturn(StatusResult.success(true));
        advancesState(AUTHORIZING, AUTHORIZED);
    }

    @Test
    void advancesStateFromAuthorizingToDenied() throws Exception {
        when(participantVerifier.isOnboardingAllowed(any())).thenReturn(StatusResult.success(false));
        advancesState(AUTHORIZING, DENIED);
    }

    @Test
    void advancesStateFromAuthorizedToOnboarded() throws Exception {
        when(verifiableCredentialService.pushVerifiableCredential(any()))
                .thenReturn(StatusResult.success());
        var participant = advancesState(AUTHORIZED, ONBOARDED);
        verify(verifiableCredentialService).pushVerifiableCredential(participant);
    }

    @Test
    void advancesStateFromAuthorizedToFailed() throws Exception {
        when(verifiableCredentialService.pushVerifiableCredential(any()))
                .thenReturn(StatusResult.failure(ResponseStatus.FATAL_ERROR));
        var participant = advancesState(AUTHORIZED, FAILED);
        verify(verifiableCredentialService).pushVerifiableCredential(participant);
    }


    @SuppressWarnings("unchecked")
    private Participant advancesState(ParticipantStatus startState, ParticipantStatus endState) throws Exception {
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
        return participant;
    }
}