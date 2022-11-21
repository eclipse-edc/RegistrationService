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

import org.eclipse.edc.iam.did.spi.credentials.CredentialsVerifier;
import org.eclipse.edc.iam.did.spi.resolution.DidResolverRegistry;
import org.eclipse.edc.policy.engine.spi.PolicyEngine;
import org.eclipse.edc.registration.authority.spi.ParticipantVerifier;
import org.eclipse.edc.registration.verifier.DefaultParticipantVerifier;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Provider;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;

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

