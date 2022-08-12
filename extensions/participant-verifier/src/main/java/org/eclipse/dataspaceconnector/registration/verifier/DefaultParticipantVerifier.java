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

package org.eclipse.dataspaceconnector.registration.verifier;

import org.eclipse.dataspaceconnector.iam.did.spi.credentials.CredentialsVerifier;
import org.eclipse.dataspaceconnector.iam.did.spi.resolution.DidResolverRegistry;
import org.eclipse.dataspaceconnector.registration.DataspaceRegistrationPolicy;
import org.eclipse.dataspaceconnector.registration.authority.spi.ParticipantVerifier;
import org.eclipse.dataspaceconnector.spi.agent.ParticipantAgent;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.policy.PolicyEngine;
import org.eclipse.dataspaceconnector.spi.response.StatusResult;

import java.util.Collections;

import static java.lang.String.format;
import static org.eclipse.dataspaceconnector.registration.DataspaceRegistrationPolicy.PARTICIPANT_REGISTRATION_SCOPE;
import static org.eclipse.dataspaceconnector.spi.response.ResponseStatus.ERROR_RETRY;

/**
 * Implementation of {@link ParticipantVerifier} that only retrieves verifiable credentials,
 * but performs no action on them. It authorizes any participant to onboard to the dataspace,
 * as long as its Identity Hub can be accessed.
 * <p>
 * This is meant as a starting point for implementing custom dataspace onboarding logic.
 */
public class DefaultParticipantVerifier implements ParticipantVerifier {

    private final Monitor monitor;
    private final DidResolverRegistry resolverRegistry;
    private final CredentialsVerifier credentialsVerifier;
    private final PolicyEngine policyEngine;
    private final DataspaceRegistrationPolicy dataspaceRegistrationPolicy;

    public DefaultParticipantVerifier(Monitor monitor, DidResolverRegistry resolverRegistry, CredentialsVerifier credentialsVerifier, PolicyEngine policyEngine, DataspaceRegistrationPolicy dataspaceRegistrationPolicy) {
        this.monitor = monitor;
        this.resolverRegistry = resolverRegistry;
        this.credentialsVerifier = credentialsVerifier;
        this.policyEngine = policyEngine;
        this.dataspaceRegistrationPolicy = dataspaceRegistrationPolicy;
    }

    @Override
    public StatusResult<Boolean> isOnboardingAllowed(String participantDid) {
        monitor.info(() -> "Get credentials VC for " + participantDid);

        var didDocument = resolverRegistry.resolve(participantDid);
        if (didDocument.failed()) {
            return StatusResult.failure(ERROR_RETRY, "Failed to resolve DID " + participantDid + ". " + didDocument.getFailureDetail());
        }

        var vcResult = credentialsVerifier.getVerifiedCredentials(didDocument.getContent());
        if (vcResult.failed()) {
            return StatusResult.failure(ERROR_RETRY, "Failed to retrieve VCs. " + vcResult.getFailureDetail());
        }
        var credentials = vcResult.getContent();

        monitor.info(() -> format("Retrieved VCs for %s: %s", participantDid, credentials));

        var agent = new ParticipantAgent(credentials, Collections.emptyMap());

        var evaluationResult = policyEngine.evaluate(PARTICIPANT_REGISTRATION_SCOPE, dataspaceRegistrationPolicy.get(), agent);
        var policyResult = evaluationResult.succeeded();

        monitor.debug(() -> "Policy evaluation result: " + policyResult);

        return StatusResult.success(policyResult);
    }
}