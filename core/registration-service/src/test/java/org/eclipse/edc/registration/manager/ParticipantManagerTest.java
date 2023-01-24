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

package org.eclipse.edc.registration.manager;

import org.eclipse.edc.registration.spi.model.Participant;
import org.eclipse.edc.registration.spi.model.ParticipantStatus;
import org.eclipse.edc.registration.spi.service.VerifiableCredentialService;
import org.eclipse.edc.registration.spi.verifier.OnboardingPolicyVerifier;
import org.eclipse.edc.registration.store.spi.ParticipantStore;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.response.ResponseStatus;
import org.eclipse.edc.spi.response.StatusResult;
import org.eclipse.edc.spi.system.ExecutorInstrumentation;
import org.eclipse.edc.spi.telemetry.Telemetry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.edc.registration.ParticipantUtils.createParticipant;
import static org.eclipse.edc.registration.spi.model.ParticipantStatus.AUTHORIZED;
import static org.eclipse.edc.registration.spi.model.ParticipantStatus.AUTHORIZING;
import static org.eclipse.edc.registration.spi.model.ParticipantStatus.DENIED;
import static org.eclipse.edc.registration.spi.model.ParticipantStatus.FAILED;
import static org.eclipse.edc.registration.spi.model.ParticipantStatus.ONBOARDED;
import static org.eclipse.edc.registration.spi.model.ParticipantStatus.ONBOARDING_INITIATED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ParticipantManagerTest {

    private final Monitor monitor = mock(Monitor.class);
    private final ParticipantStore participantStore = mock(ParticipantStore.class);
    private final OnboardingPolicyVerifier participantVerifier = mock(OnboardingPolicyVerifier.class);
    private final VerifiableCredentialService verifiableCredentialService = mock(VerifiableCredentialService.class);
    private final Participant.Builder participantBuilder = createParticipant();
    private final ArgumentCaptor<Participant> captor = ArgumentCaptor.forClass(Participant.class);

    private ParticipantManager manager;

    @BeforeEach
    void setUp() {
        manager = new ParticipantManager(monitor, participantStore, participantVerifier, ExecutorInstrumentation.noop(), verifiableCredentialService, new Telemetry());
    }

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

    @ParameterizedTest
    @EnumSource(ResponseStatus.class)
    void advancesStateFromAuthorizingToFailed(ResponseStatus errorStatus) throws Exception {
        when(participantVerifier.isOnboardingAllowed(any())).thenReturn(StatusResult.failure(errorStatus));
        advancesState(AUTHORIZING, FAILED);
    }

    @ParameterizedTest
    @EnumSource(ResponseStatus.class)
    void advancesStateFromAuthorizedToFailed(ResponseStatus errorStatus) throws Exception {
        when(verifiableCredentialService.pushVerifiableCredential(any()))
                .thenReturn(StatusResult.failure(errorStatus));
        advancesState(AUTHORIZED, FAILED);
    }

    @Test
    void advancesStateFromAuthorizedToOnboarded() throws Exception {
        when(verifiableCredentialService.pushVerifiableCredential(any()))
                .thenReturn(StatusResult.success());
        advancesState(AUTHORIZED, ONBOARDED);
    }

    private Participant advancesState(ParticipantStatus startState, ParticipantStatus endState) throws Exception {
        var participant = participantBuilder.status(startState).build();
        when(participantStore.listParticipantsWithStatus(eq(startState))).thenReturn(List.of(participant), List.of());
        var latch = new CountDownLatch(1);
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(participantStore).save(any());

        manager.start();
        assertThat(latch.await(10, SECONDS)).isTrue();

        verify(participantStore).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(endState);
        assertThat(captor.getValue())
                .usingRecursiveComparison()
                .ignoringFieldsOfTypes(ParticipantStatus.class)
                .isEqualTo(participant);

        manager.stop();
        return participant;
    }
}
