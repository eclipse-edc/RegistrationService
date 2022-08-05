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

import com.github.javafaker.Faker;
import org.eclipse.dataspaceconnector.iam.did.spi.credentials.CredentialsVerifier;
import org.eclipse.dataspaceconnector.iam.did.spi.document.DidDocument;
import org.eclipse.dataspaceconnector.iam.did.spi.resolution.DidResolverRegistry;
import org.eclipse.dataspaceconnector.policy.model.Policy;
import org.eclipse.dataspaceconnector.registration.authority.model.Participant;
import org.eclipse.dataspaceconnector.registration.store.spi.ParticipantStore;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.policy.PolicyEngine;
import org.eclipse.dataspaceconnector.spi.result.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.dataspaceconnector.registration.TestUtils.createParticipant;
import static org.eclipse.dataspaceconnector.registration.api.RegistrationServiceImpl.ONBOARDING_SCOPE;
import static org.eclipse.dataspaceconnector.registration.authority.model.ParticipantStatus.ONBOARDING_INITIATED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RegistrationServiceTest {
    static final Faker FAKER = new Faker();

    private final Monitor monitor = mock(Monitor.class);
    private final ParticipantStore participantStore = mock(ParticipantStore.class);
    private final PolicyEngine policyEngine = mock(PolicyEngine.class);

    private final Policy dataspacePolicy = Policy.Builder.newInstance().build();
    private final CredentialsVerifier verifier = mock(CredentialsVerifier.class);
    private final Participant.Builder participantBuilder = createParticipant();
    private final String did = FAKER.internet().url();
    private final String idsUrl = FAKER.internet().url();
    private final DidResolverRegistry didResolverRegistry = mock(DidResolverRegistry.class);
    private RegistrationService service;

    @BeforeEach
    void setup() {
        when(policyEngine.evaluate(any(), any(), any())).thenReturn(Result.success(Policy.Builder.newInstance().build()));
        when(didResolverRegistry.resolve(eq(did))).thenReturn(Result.success(new DidDocument()));
        when(verifier.getVerifiedCredentials(any())).thenReturn(Result.success(Map.of("region", "eu")));
        service = new RegistrationServiceImpl(monitor, participantStore, policyEngine, dataspacePolicy, verifier, didResolverRegistry);
    }

    @Test
    void listParticipants_empty() {
        assertThat(service.listParticipants()).isEmpty();
    }

    @Test
    void listParticipants() {
        var participant = participantBuilder.build();
        when(participantStore.listParticipants()).thenReturn(List.of(participant));
        assertThat(service.listParticipants()).containsExactly(participant);
    }

    @Test
    void addParticipant() {
        var result = service.addParticipant(did, idsUrl);

        var captor = ArgumentCaptor.forClass(Participant.class);
        verify(participantStore).save(captor.capture());
        assertThat(result.succeeded()).isTrue();
        assertThat(captor.getValue())
                .usingRecursiveComparison()
                .isEqualTo(Participant.Builder.newInstance()
                        .did(did)
                        .status(ONBOARDING_INITIATED)
                        .name(did)
                        .url(idsUrl)
                        .supportedProtocol("ids-multipart")
                        .build());
    }

    @Test
    void addParticipant_whenPolicyNotSatisfied_shouldFail() {
        when(policyEngine.evaluate(eq(ONBOARDING_SCOPE), any(), any())).thenReturn(Result.failure("foo"));

        var result = service.addParticipant(did, idsUrl);
        assertThat(result.failed()).isTrue();
    }

    @Test
    void addParticipant_didNotResolved_shouldFail() {
        when(didResolverRegistry.resolve(eq(did))).thenReturn(Result.failure("not found"));

        var voidResult = service.addParticipant(did, idsUrl);
        assertThat(voidResult.failed()).isTrue();
    }

    @Test
    void addParticipant_claimsVerificationFails_shouldFail() {
        when(verifier.getVerifiedCredentials(any())).thenReturn(Result.failure("not verified"));
        var result = service.addParticipant(did, idsUrl);
        assertThat(result.failed()).isTrue();
    }

    @Test
    void addParticipant_noClaims_shouldFail() {
        when(verifier.getVerifiedCredentials(any())).thenReturn(Result.success(Collections.emptyMap()));
        var result = service.addParticipant(did, idsUrl);
        assertThat(result.failed()).isTrue();
    }
}
