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

package org.eclipse.dataspaceconnector.registration.authority;

import com.github.javafaker.Faker;
import org.assertj.core.api.AbstractStringAssert;
import org.eclipse.dataspaceconnector.iam.did.spi.credentials.CredentialsVerifier;
import org.eclipse.dataspaceconnector.iam.did.spi.document.DidDocument;
import org.eclipse.dataspaceconnector.iam.did.spi.document.Service;
import org.eclipse.dataspaceconnector.iam.did.spi.resolution.DidResolverRegistry;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.response.ResponseStatus;
import org.eclipse.dataspaceconnector.spi.result.Result;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.dataspaceconnector.spi.response.ResponseStatus.ERROR_RETRY;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DefaultParticipantVerifierTest {
    static final Faker FAKER = new Faker();
    static final String IDENTITY_HUB_TYPE = "IdentityHub";

    Monitor monitor = mock(Monitor.class);
    String participantDid = FAKER.internet().url();
    DidResolverRegistry resolverRegistry = mock(DidResolverRegistry.class);
    CredentialsVerifier credentialsVerifier = mock(CredentialsVerifier.class);
    String identityHubUrl = FAKER.internet().url();
    String failure = FAKER.lorem().sentence();
    Map<String, Object> verifiableCredentials = mock(Map.class);
    DefaultParticipantVerifier service = new DefaultParticipantVerifier(monitor, resolverRegistry, credentialsVerifier);

    @BeforeEach
    void beforeEach() {
        DidDocument didDocument = DidDocument.Builder.newInstance()
                .service(List.of(new Service(FAKER.lorem().word(), IDENTITY_HUB_TYPE, identityHubUrl)))
                .build();
        when(resolverRegistry.resolve(participantDid))
                .thenReturn(Result.success(didDocument));
        when(credentialsVerifier.getVerifiedCredentials(didDocument))
                .thenReturn(Result.success(verifiableCredentials));
    }

    @Test
    void verifyCredentials_createsMembershipCredential() {
        var result = service.verifyCredentials(participantDid);
        assertThat(result.succeeded()).isTrue();
        assertThat(result.getContent()).isTrue();
    }

    @Test
    void verifyCredentials_whenDidNotResolved_throws() {
        when(resolverRegistry.resolve(participantDid))
                .thenReturn(Result.failure(failure));

        assertThatCallFailsWith(ERROR_RETRY)
                .isEqualTo(format("Failed to resolve DID %s. %s", participantDid, failure));
    }

    @Test
    void verifyCredentials_whenPushToIdentityHubFails_throws() {
        when(credentialsVerifier.getVerifiedCredentials(any()))
                .thenReturn(Result.failure(failure));

        assertThatCallFailsWith(ERROR_RETRY)
                .isEqualTo(format("Failed to retrieve VCs. %s", failure));
    }

    @NotNull
    private AbstractStringAssert<?> assertThatCallFailsWith(ResponseStatus status) {
        var result = service.verifyCredentials(participantDid);
        assertThat(result.failed()).isTrue();
        assertThat(result.getFailure().status()).isEqualTo(status);
        return assertThat(result.getFailureDetail());
    }
}