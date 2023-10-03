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

package org.eclipse.edc.registration.verifier;

import org.eclipse.edc.iam.did.spi.credentials.CredentialsVerifier;
import org.eclipse.edc.iam.did.spi.resolution.DidResolverRegistry;
import org.eclipse.edc.policy.engine.spi.PolicyContextImpl;
import org.eclipse.edc.policy.engine.spi.PolicyEngine;
import org.eclipse.edc.registration.spi.registration.DataspaceRegistrationPolicy;
import org.eclipse.edc.registration.spi.verifier.OnboardingPolicyVerifier;
import org.eclipse.edc.spi.agent.ParticipantAgent;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.response.StatusResult;

import java.util.Collections;

import static java.lang.String.format;
import static org.eclipse.edc.registration.spi.registration.DataspaceRegistrationPolicy.PARTICIPANT_REGISTRATION_SCOPE;
import static org.eclipse.edc.spi.response.ResponseStatus.ERROR_RETRY;

/**
 * Implementation of {@link OnboardingPolicyVerifier} that checks whether a participant can be onboarded into the dataspace.
 * More specifically it fetches the Verifiable Credentials from the participant Identity Hub and evaluates them
 * against the provided {@link DataspaceRegistrationPolicy}.
 */
public class OnboardingPolicyVerifierImpl implements OnboardingPolicyVerifier {

    private final Monitor monitor;
    private final DidResolverRegistry resolverRegistry;
    private final CredentialsVerifier credentialsVerifier;
    private final PolicyEngine policyEngine;
    private final DataspaceRegistrationPolicy dataspaceRegistrationPolicy;

    public OnboardingPolicyVerifierImpl(Monitor monitor, DidResolverRegistry resolverRegistry, CredentialsVerifier credentialsVerifier, PolicyEngine policyEngine, DataspaceRegistrationPolicy dataspaceRegistrationPolicy) {
        this.monitor = monitor;
        this.resolverRegistry = resolverRegistry;
        this.credentialsVerifier = credentialsVerifier;
        this.policyEngine = policyEngine;
        this.dataspaceRegistrationPolicy = dataspaceRegistrationPolicy;
    }

    /**
     * Fetches Verifiable Credentials from the participant Identity Hub and evaluates them against the provided {@link DataspaceRegistrationPolicy}
     * <p>
     * The method returns a {@link StatusResult#succeeded() succeeded} {@link StatusResult} if the policy could be evaluated.
     * * The {@link Boolean boolean} payload then contains the policy evaluation result (allowed or denied).
     * * A {@link StatusResult#failed() failed} result indicates that the policy could not be evaluated,
     * * for example, because Verifiable Credentials could not be retrieved from the Identity Hub.
     * *
     * * @param participantDid The putative participant's DID URL. Used to resolve participant Identity Hub and retrieve Verifiable Credentials.
     * * @return a result indicating whether the policy could be evaluated, and if successful, the evaluation result.
     */
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

        var policyContext = PolicyContextImpl.Builder.newInstance()
                .additional(ParticipantAgent.class, agent)
                .build();

        var evaluationResult = policyEngine.evaluate(PARTICIPANT_REGISTRATION_SCOPE, dataspaceRegistrationPolicy.get(), policyContext);
        var policyResult = evaluationResult.succeeded();

        monitor.debug(() -> "Policy evaluation result: " + policyResult);

        return StatusResult.success(policyResult);
    }
}
