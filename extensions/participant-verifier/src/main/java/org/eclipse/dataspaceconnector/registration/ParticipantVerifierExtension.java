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

package org.eclipse.dataspaceconnector.registration;

import org.eclipse.dataspaceconnector.iam.did.spi.credentials.CredentialsVerifier;
import org.eclipse.dataspaceconnector.iam.did.spi.resolution.DidResolverRegistry;
import org.eclipse.dataspaceconnector.registration.authority.spi.ParticipantVerifier;
import org.eclipse.dataspaceconnector.registration.verifier.DefaultParticipantVerifier;
import org.eclipse.dataspaceconnector.runtime.metamodel.annotation.Inject;
import org.eclipse.dataspaceconnector.runtime.metamodel.annotation.Provider;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.policy.engine.PolicyEngine;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext;

/**
 * EDC extension to boot the {@link ParticipantVerifier} used by the Authority Service.
 */
public class ParticipantVerifierExtension implements ServiceExtension {

    @Inject
    private Monitor monitor;

    @Inject
    private DidResolverRegistry didResolverRegistry;

    @Inject
    private CredentialsVerifier credentialsVerifier;

    @Inject
    private PolicyEngine policyEngine;

    @Inject
    private DataspaceRegistrationPolicy dataspaceRegistrationPolicy;

    @Provider
    public ParticipantVerifier participantVerifier(ServiceExtensionContext context) {
        return new DefaultParticipantVerifier(monitor, didResolverRegistry, credentialsVerifier, policyEngine, dataspaceRegistrationPolicy);
    }
}

