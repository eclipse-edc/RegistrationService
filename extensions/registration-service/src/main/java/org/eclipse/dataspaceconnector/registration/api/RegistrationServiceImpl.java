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

package org.eclipse.dataspaceconnector.registration.api;

import org.eclipse.dataspaceconnector.iam.did.spi.credentials.CredentialsVerifier;
import org.eclipse.dataspaceconnector.iam.did.spi.document.DidDocument;
import org.eclipse.dataspaceconnector.iam.did.spi.resolution.DidResolverRegistry;
import org.eclipse.dataspaceconnector.policy.model.Policy;
import org.eclipse.dataspaceconnector.registration.authority.model.Participant;
import org.eclipse.dataspaceconnector.registration.store.spi.ParticipantStore;
import org.eclipse.dataspaceconnector.spi.agent.ParticipantAgent;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.policy.PolicyEngine;
import org.eclipse.dataspaceconnector.spi.result.Result;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.eclipse.dataspaceconnector.registration.authority.model.ParticipantStatus.ONBOARDING_INITIATED;

/**
 * Registration service for dataspace participants.
 */
public class RegistrationServiceImpl implements RegistrationService {

    private final Monitor monitor;
    private final ParticipantStore participantStore;
    private final PolicyEngine policyEngine;
    private final Policy dataspacePolicy;
    private final CredentialsVerifier verifier;
    private final DidResolverRegistry didResolverRegistry;

    public RegistrationServiceImpl(Monitor monitor, ParticipantStore participantStore, PolicyEngine policyEngine, Policy dataspacePolicy, CredentialsVerifier verifier, DidResolverRegistry didResolverRegistry) {
        this.monitor = monitor;
        this.participantStore = participantStore;
        this.policyEngine = policyEngine;
        this.dataspacePolicy = dataspacePolicy;
        this.verifier = verifier;
        this.didResolverRegistry = didResolverRegistry;
    }

    /**
     * Lists all dataspace participants.
     *
     * @return list of dataspace participants.
     */
    @Override
    public List<Participant> listParticipants() {
        monitor.info("List all participants of the dataspace.");
        return new ArrayList<>(participantStore.listParticipants());
    }

    /**
     * Add a participant to a dataspace.
     * <p>
     * In a future version, the {@code idsUrl} argument will be removed, as the {@code did} provides sufficient
     * information to identify the participant, and the Registration Service will not manage service URLs.
     *
     * @param did the DID of the dataspace participant to add.
     * @param idsUrl the IDS URL of the dataspace participant to add.
     */
    @Override
    public Result<Void> addParticipant(String did, String idsUrl) {
        monitor.info("Adding a participant in the dataspace.");

        var claims = getClaimsFromDid(did);
        var pa = new ParticipantAgent(claims, Collections.emptyMap());

        var evaluationResult = policyEngine.evaluate("PARTICIPANT_REGISTRATION", dataspacePolicy, pa);
        if (evaluationResult.failed()) {
            return Result.failure(evaluationResult.getFailureMessages());
        }
        var participant = Participant.Builder.newInstance()
                .did(did)
                .status(ONBOARDING_INITIATED)
                .name(did)
                .url(idsUrl)
                .supportedProtocol("ids-multipart")
                .build();

        participantStore.save(participant);
        return Result.success();
    }

    private Map<String, Object> getClaimsFromDid(String did) {
        var didDocumentResult = getDidDocument(did);
        if(didDocumentResult.failed()){
            throw new IllegalStateException("DID could not be resolved: " + did);
        }
        var result = verifier.getVerifiedCredentials(didDocumentResult.getContent());
        return result.succeeded() ? result.getContent() : Collections.emptyMap();
    }

    private Result<DidDocument> getDidDocument(String did) {
        return didResolverRegistry.resolve(did);
    }
}
