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

import io.opentelemetry.extension.annotations.WithSpan;
import org.eclipse.dataspaceconnector.common.statemachine.StateMachineManager;
import org.eclipse.dataspaceconnector.common.statemachine.StateProcessorImpl;
import org.eclipse.dataspaceconnector.registration.authority.model.Participant;
import org.eclipse.dataspaceconnector.registration.authority.model.ParticipantStatus;
import org.eclipse.dataspaceconnector.registration.authority.spi.ParticipantVerifier;
import org.eclipse.dataspaceconnector.registration.credential.VerifiableCredentialService;
import org.eclipse.dataspaceconnector.registration.store.spi.ParticipantStore;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.retry.WaitStrategy;
import org.eclipse.dataspaceconnector.spi.system.ExecutorInstrumentation;
import org.eclipse.dataspaceconnector.spi.telemetry.Telemetry;

import java.util.function.Function;

import static org.eclipse.dataspaceconnector.registration.authority.model.ParticipantStatus.AUTHORIZED;
import static org.eclipse.dataspaceconnector.registration.authority.model.ParticipantStatus.AUTHORIZING;
import static org.eclipse.dataspaceconnector.registration.authority.model.ParticipantStatus.ONBOARDING_INITIATED;

/**
 * Manager for participant registration state machine.
 */
public class ParticipantManager {

    private final ParticipantStore participantStore;
    private final ParticipantVerifier participantVerifier;
    private final StateMachineManager stateMachineManager;
    private final VerifiableCredentialService verifiableCredentialService;
    private final Telemetry telemetry;

    public ParticipantManager(Monitor monitor, ParticipantStore participantStore, ParticipantVerifier participantVerifier, ExecutorInstrumentation executorInstrumentation,
                              VerifiableCredentialService verifiableCredentialService, Telemetry telemetry) {
        this.participantStore = participantStore;
        this.participantVerifier = participantVerifier;
        this.verifiableCredentialService = verifiableCredentialService;
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
        var result = verifiableCredentialService.pushVerifiableCredential(participant);
        if (result.succeeded()) {
            participant.transitionOnboarded();
        } else {
            participant.transitionFailed();
        }

        participantStore.save(participant);
        return true;
    }

    private StateProcessorImpl<Participant> processParticipantsInState(ParticipantStatus status, Function<Participant, Boolean> function) {
        var functionWithTraceContext = telemetry.contextPropagationMiddleware(function);
        return new StateProcessorImpl<>(() -> participantStore.listParticipantsWithStatus(status), functionWithTraceContext);
    }
}
