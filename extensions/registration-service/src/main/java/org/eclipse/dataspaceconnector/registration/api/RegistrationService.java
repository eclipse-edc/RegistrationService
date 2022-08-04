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
import org.eclipse.dataspaceconnector.policy.model.Policy;
import org.eclipse.dataspaceconnector.registration.authority.model.Participant;
import org.eclipse.dataspaceconnector.registration.store.spi.ParticipantStore;
import org.eclipse.dataspaceconnector.spi.agent.ParticipantAgent;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.policy.PolicyEngine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.eclipse.dataspaceconnector.registration.authority.model.ParticipantStatus.ONBOARDING_INITIATED;

/**
 * Registration service for dataspace participants.
 */
public class RegistrationService {

    private final Monitor monitor;
    private final ParticipantStore participantStore;
    private PolicyEngine policyEngine;
    private Policy dummyPolicy;
    private CredentialsVerifier verifier;

    public RegistrationService(Monitor monitor, ParticipantStore participantStore) {
        this.monitor = monitor;
        this.participantStore = participantStore;
    }

    /**
     * Lists all dataspace participants.
     *
     * @return list of dataspace participants.
     */
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
    public void addParticipant(String did, String idsUrl) {
        monitor.info("Adding a participant in the dataspace.");

        var claims = getClaimsFromDid(did);
        var pa = new ParticipantAgent(claims, Collections.emptyMap());

        var evalutationResult = policyEngine.evaluate("PARTICIPANT_REGISTRATION", dummyPolicy, pa);
        if(evalutationResult.failed()){
            throw new IsNotGaiaXMameberException();
        }
        var participant = Participant.Builder.newInstance()
                .did(did)
                .status(ONBOARDING_INITIATED)
                .name(did)
                .url(idsUrl)
                .supportedProtocol("ids-multipart")
                .build();

        participantStore.save(participant);
    }

    private Map<String, Object> getClaimsFromDid(String did) {
        var result = verifier.getVerifiedCredentials(getDidDocument(did));
        return result.succeeded() ? result.getContent() : Collections.emptyMap();
    }

    private DidDocument getDidDocument(String did) {
        return null;
    }
}
