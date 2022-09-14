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

package org.eclipse.dataspaceconnector.registration.credential;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import org.assertj.core.api.AbstractStringAssert;
import org.eclipse.dataspaceconnector.iam.did.spi.document.DidDocument;
import org.eclipse.dataspaceconnector.iam.did.spi.document.Service;
import org.eclipse.dataspaceconnector.iam.did.spi.key.PrivateKeyWrapper;
import org.eclipse.dataspaceconnector.iam.did.spi.resolution.DidResolverRegistry;
import org.eclipse.dataspaceconnector.identityhub.client.IdentityHubClient;
import org.eclipse.dataspaceconnector.identityhub.credentials.VerifiableCredentialsJwtService;
import org.eclipse.dataspaceconnector.identityhub.credentials.model.VerifiableCredential;
import org.eclipse.dataspaceconnector.registration.authority.model.Participant;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.response.ResponseStatus;
import org.eclipse.dataspaceconnector.spi.response.StatusResult;
import org.eclipse.dataspaceconnector.spi.result.Result;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.dataspaceconnector.registration.authority.TestUtils.createParticipant;
import static org.eclipse.dataspaceconnector.spi.response.ResponseStatus.ERROR_RETRY;
import static org.eclipse.dataspaceconnector.spi.response.ResponseStatus.FATAL_ERROR;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VerifiableCredentialServiceImplTest {
    static final String IDENTITY_HUB_TYPE = "IdentityHub";

    Monitor monitor = mock(Monitor.class);
    VerifiableCredentialsJwtService jwtService = mock(VerifiableCredentialsJwtService.class);
    PrivateKeyWrapper privateKeyWrapper = mock(PrivateKeyWrapper.class);
    String dataspaceDid = "some.test/url";
    String participantDid = "some.test/url";
    DidResolverRegistry resolverRegistry = mock(DidResolverRegistry.class);
    IdentityHubClient identityHubClient = mock(IdentityHubClient.class);
    SignedJWT jwt = mock(SignedJWT.class);
    String identityHubUrl = "some.test/url";
    String failure = "Test Failure";
    VerifiableCredentialServiceImpl service = new VerifiableCredentialServiceImpl(monitor, jwtService, privateKeyWrapper, dataspaceDid, resolverRegistry, identityHubClient);
    Participant.Builder participantBuilder = createParticipant().did(participantDid);
    ArgumentCaptor<VerifiableCredential> vc = ArgumentCaptor.forClass(VerifiableCredential.class);

    @BeforeEach
    void beforeEach() throws Exception {
        when(resolverRegistry.resolve(participantDid))
                .thenReturn(Result.success(DidDocument.Builder.newInstance()
                        .service(List.of(new Service(UUID.randomUUID().toString(), IDENTITY_HUB_TYPE, identityHubUrl)))
                        .build()));
        when(jwtService.buildSignedJwt(any(), eq(dataspaceDid), eq(participantDid), eq(privateKeyWrapper)))
                .thenReturn(jwt);
        when(identityHubClient.addVerifiableCredential(identityHubUrl, jwt))
                .thenReturn(StatusResult.success());
    }

    @Test
    void pushVerifiableCredential_createsMembershipCredential() throws Exception {
        var result = service.pushVerifiableCredential(participantBuilder.build());
        assertThat(result.succeeded()).isTrue();

        verify(jwtService).buildSignedJwt(vc.capture(), eq(dataspaceDid), eq(participantDid), eq(privateKeyWrapper));
        assertThat(vc.getValue().getId()).satisfies(i -> assertThat(UUID.fromString(i)).isNotNull());
        assertThat(vc.getValue().getCredentialSubject()).isEqualTo(Map.of("memberOfDataspace", dataspaceDid));
    }

    @Test
    void pushVerifiableCredential_pushesCredential() {
        service.pushVerifiableCredential(participantBuilder.build());

        verify(identityHubClient).addVerifiableCredential(identityHubUrl, jwt);
    }

    @Test
    void pushVerifiableCredential_whenDidNotResolved_throws() {
        when(resolverRegistry.resolve(participantDid))
                .thenReturn(Result.failure(failure));

        assertThatCallFailsWith(ERROR_RETRY)
                .isEqualTo(format("Failed to resolve DID %s. %s", participantDid, failure));
    }

    @Test
    void pushVerifiableCredential_whenDidDocumentDoesNotContainHubUrl_throws() {
        when(resolverRegistry.resolve(participantDid))
                .thenReturn(Result.success(DidDocument.Builder.newInstance()
                        .service(List.of(new Service(UUID.randomUUID().toString(), "test-type", identityHubUrl)))
                        .build()));

        assertThatCallFailsWith(FATAL_ERROR)
                .isEqualTo(format("Failed to resolve Identity Hub URL from DID document for %s", participantDid));
    }

    @Test
    void pushVerifiableCredential_whenJwtCannotBeSigned_throws() throws Exception {
        when(jwtService.buildSignedJwt(any(), eq(dataspaceDid), eq(participantDid), eq(privateKeyWrapper)))
                .thenThrow(new JOSEException(failure));

        assertThatCallFailsWith(FATAL_ERROR)
                .isEqualTo(format("%s: %s", JOSEException.class.getCanonicalName(), failure));
    }

    @Test
    void pushVerifiableCredential_whenPushToIdentityHubFails_throws() {
        when(identityHubClient.addVerifiableCredential(identityHubUrl, jwt))
                .thenReturn(StatusResult.failure(FATAL_ERROR, failure));

        assertThatCallFailsWith(ERROR_RETRY)
                .isEqualTo(format("Failed to send VC. %s", failure));
    }

    @NotNull
    private AbstractStringAssert<?> assertThatCallFailsWith(ResponseStatus status) {
        StatusResult<Void> result = service.pushVerifiableCredential(participantBuilder.build());
        assertThat(result.failed()).isTrue();
        assertThat(result.getFailure().status()).isEqualTo(status);
        return assertThat(result.getFailureDetail());
    }
}