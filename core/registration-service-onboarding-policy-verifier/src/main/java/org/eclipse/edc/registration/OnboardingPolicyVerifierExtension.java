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
import org.eclipse.edc.registration.spi.registration.DataspaceRegistrationPolicy;
import org.eclipse.edc.registration.spi.verifier.OnboardingPolicyVerifier;
import org.eclipse.edc.registration.verifier.OnboardingPolicyVerifierImpl;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Provider;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.system.ServiceExtension;

@Extension(value = OnboardingPolicyVerifierExtension.NAME)
public class OnboardingPolicyVerifierExtension implements ServiceExtension {

    public static final String NAME = "Onboarding Policy Verifier";

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

    @Override
    public String name() {
        return NAME;
    }

    @Provider
    public OnboardingPolicyVerifier onboardingPolicyVerifier() {
        return new OnboardingPolicyVerifierImpl(monitor, didResolverRegistry, credentialsVerifier, policyEngine, dataspaceRegistrationPolicy);
    }
}

