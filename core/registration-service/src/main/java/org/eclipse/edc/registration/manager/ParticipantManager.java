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

import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.eclipse.edc.registration.spi.model.Participant;
import org.eclipse.edc.registration.spi.model.ParticipantStatus;
import org.eclipse.edc.registration.spi.service.VerifiableCredentialService;
import org.eclipse.edc.registration.spi.verifier.OnboardingPolicyVerifier;
import org.eclipse.edc.registration.store.spi.ParticipantStore;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.retry.WaitStrategy;
import org.eclipse.edc.spi.system.ExecutorInstrumentation;
import org.eclipse.edc.spi.telemetry.Telemetry;
import org.eclipse.edc.statemachine.ProcessorImpl;
import org.eclipse.edc.statemachine.StateMachineManager;

import java.util.function.Function;

import static org.eclipse.edc.registration.spi.model.ParticipantStatus.AUTHORIZED;
import static org.eclipse.edc.registration.spi.model.ParticipantStatus.AUTHORIZING;
import static org.eclipse.edc.registration.spi.model.ParticipantStatus.ONBOARDING_INITIATED;

/**
 * Manager for participant registration state machine.
 */
public class ParticipantManager {

    private final ParticipantStore participantStore;
    private final OnboardingPolicyVerifier participantVerifier;
    private final StateMachineManager stateMachineManager;
    private final VerifiableCredentialService vcService;
    private final Telemetry telemetry;

    public ParticipantManager(Monitor monitor, ParticipantStore participantStore, OnboardingPolicyVerifier participantVerifier, ExecutorInstrumentation executorInstrumentation,
                              VerifiableCredentialService vcService, Telemetry telemetry) {
        this.participantStore = participantStore;
        this.participantVerifier = participantVerifier;
        this.vcService = vcService;
        this.telemetry = telemetry;

        // default wait five seconds
        WaitStrategy waitStrategy = () -> 5000L;

        // define state machine
        stateMachineManager = StateMachineManager.Builder.newInstance("registration-service", monitor, executorInstrumentation, waitStrategy)
                .processor(processParticipantsInState(ONBOARDING_INITIATED, this::processOnboardingInitiated))
                .processor(processParticipantsInState(AUTHORIZING, this::processAuthorizing))
                .processor(processParticipantsInState(AUTHORIZED, this::processAuthorized))
                .build();
    }

    /**
     * Start the participant manager state machine processor thread.
     */
    public void start() {
        stateMachineManager.start();
    }

    /**
     * Stop the participant manager state machine processor thread.
     */
    public void stop() {
        stateMachineManager.stop();
    }

    @WithSpan
    private Boolean processOnboardingInitiated(Participant participant) {
        participant.transitionAuthorizing();
        participantStore.save(participant);
        return true;
    }

    @WithSpan
    private Boolean processAuthorizing(Participant participant) {
        var credentialsValid = participantVerifier.isOnboardingAllowed(participant.getDid());
        if (credentialsValid.failed()) {
            participant.transitionFailed();
        } else if (credentialsValid.getContent()) {
            participant.transitionAuthorized();
        } else {
            participant.transitionDenied();
        }
        participantStore.save(participant);
        return true;
    }

    @WithSpan
    private Boolean processAuthorized(Participant participant) {
        var result = vcService.pushVerifiableCredential(participant);
        if (result.succeeded()) {
            participant.transitionOnboarded();
        } else {
            participant.transitionFailed();
        }

        participantStore.save(participant);
        return true;
    }
    

    private ProcessorImpl<Participant> processParticipantsInState(ParticipantStatus status, Function<Participant, Boolean> function) {
        return ProcessorImpl.Builder.newInstance(() -> participantStore.listParticipantsWithStatus(status))
                .process(telemetry.contextPropagationMiddleware(function))
                .build();
    }
}
