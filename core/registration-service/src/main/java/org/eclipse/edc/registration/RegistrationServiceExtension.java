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

package org.eclipse.edc.registration;

import org.eclipse.edc.registration.manager.ParticipantManager;
import org.eclipse.edc.registration.service.RegistrationServiceImpl;
import org.eclipse.edc.registration.spi.registration.RegistrationService;
import org.eclipse.edc.registration.spi.service.VerifiableCredentialService;
import org.eclipse.edc.registration.spi.verifier.OnboardingPolicyVerifier;
import org.eclipse.edc.registration.store.InMemoryParticipantStore;
import org.eclipse.edc.registration.store.spi.ParticipantStore;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Provider;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.response.StatusResult;
import org.eclipse.edc.spi.system.ExecutorInstrumentation;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.telemetry.Telemetry;
import org.eclipse.edc.transaction.spi.TransactionContext;

@Extension(RegistrationServiceExtension.NAME)
public class RegistrationServiceExtension implements ServiceExtension {

    public static final String NAME = "Registration Service";

    @Inject
    private Monitor monitor;

    @Inject
    private ParticipantStore participantStore;

    @Inject
    private OnboardingPolicyVerifier participantVerifier;

    @Inject
    private ExecutorInstrumentation executorInstrumentation;

    @Inject
    private VerifiableCredentialService vcService;

    @Inject
    private Telemetry telemetry;

    @Inject
    private TransactionContext transactionContext;

    private ParticipantManager participantManager;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        participantManager = new ParticipantManager(monitor, participantStore, participantVerifier, executorInstrumentation, vcService, telemetry);
    }

    @Override
    public void start() {
        participantManager.start();
    }

    @Override
    public void shutdown() {
        participantManager.stop();
    }

    @Provider
    public RegistrationService registrationService() {
        return new RegistrationServiceImpl(monitor, participantStore, telemetry, transactionContext);
    }

    @Provider(isDefault = true)
    public ParticipantStore participantStore() {
        return new InMemoryParticipantStore();
    }

    @Provider(isDefault = true)
    public OnboardingPolicyVerifier participantVerifier() {
        return did -> StatusResult.success(true);
    }

    @Provider(isDefault = true)
    public VerifiableCredentialService verifiableCredentialService() {
        return participant -> StatusResult.success();
    }
}
