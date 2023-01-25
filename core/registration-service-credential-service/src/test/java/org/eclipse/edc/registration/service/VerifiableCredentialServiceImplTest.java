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

package org.eclipse.edc.registration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import org.assertj.core.api.AbstractStringAssert;
import org.eclipse.edc.iam.did.spi.document.DidDocument;
import org.eclipse.edc.iam.did.spi.document.Service;
import org.eclipse.edc.iam.did.spi.key.PrivateKeyWrapper;
import org.eclipse.edc.iam.did.spi.resolution.DidResolverRegistry;
import org.eclipse.edc.identityhub.client.spi.IdentityHubClient;
import org.eclipse.edc.identityhub.credentials.jwt.JwtCredentialEnvelope;
import org.eclipse.edc.identityhub.credentials.jwt.JwtCredentialFactory;
import org.eclipse.edc.identityhub.spi.credentials.model.Credential;
import org.eclipse.edc.identityhub.spi.credentials.model.CredentialEnvelope;
import org.eclipse.edc.registration.spi.model.Participant;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.response.ResponseStatus;
import org.eclipse.edc.spi.response.StatusResult;
import org.eclipse.edc.spi.result.Result;
import org.eclipse.edc.spi.types.TypeManager;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;

import java.text.ParseException;
import java.util.List;
import java.util.UUID;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.edc.registration.ParticipantUtils.createParticipant;
import static org.eclipse.edc.spi.response.ResponseStatus.ERROR_RETRY;
import static org.eclipse.edc.spi.response.ResponseStatus.FATAL_ERROR;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VerifiableCredentialServiceImplTest {

    private static final String IDENTITY_HUB_TYPE = "IdentityHub";
    private static final String DATASPACE_DID = "some.test/url";
    private static final String PARTICIPANT_DID = "some.test/url";
    private static final String IDENTITY_HUB_URL = "some.test/url";
    private static final String FAILURE_MESSAGE = "Test Failure";

    private static final ObjectMapper MAPPER = new TypeManager().getMapper();

    private final Monitor monitor = mock(Monitor.class);
    private final DidResolverRegistry resolverRegistry = mock(DidResolverRegistry.class);
    private final IdentityHubClient identityHubClient = mock(IdentityHubClient.class);
    private final JwtCredentialFactory jwtCredentialFactory = mock(JwtCredentialFactory.class);
    private final SignedJWT jwt = mock(SignedJWT.class);
    private final PrivateKeyWrapper privateKeyWrapper = mock(PrivateKeyWrapper.class);
    private final Credential credential = mock(Credential.class);

    private VerifiableCredentialServiceImpl service;

    @BeforeEach
    void setUp() throws ParseException, JOSEException {
        when(resolverRegistry.resolve(PARTICIPANT_DID))
                .thenReturn(Result.success(DidDocument.Builder.newInstance()
                        .service(List.of(new Service(UUID.randomUUID().toString(), IDENTITY_HUB_TYPE, IDENTITY_HUB_URL)))
                        .build()));

        when(jwtCredentialFactory.buildSignedJwt(credential, privateKeyWrapper)).thenReturn(jwt);

        service = new VerifiableCredentialServiceImpl(monitor, privateKeyWrapper, resolverRegistry, identityHubClient, p -> Result.success(credential), jwtCredentialFactory);
    }

    @Test
    void pushVerifiableCredential() throws ParseException, JOSEException {
        var captor = ArgumentCaptor.forClass(CredentialEnvelope.class);
        when(identityHubClient.addVerifiableCredential(eq(IDENTITY_HUB_URL), captor.capture()))
                .thenReturn(Result.success());

        var result = service.pushVerifiableCredential(participant());

        assertThat(result.succeeded()).isTrue();

        verify(jwtCredentialFactory).buildSignedJwt(credential, privateKeyWrapper);
        verify(identityHubClient).addVerifiableCredential(eq(IDENTITY_HUB_URL), argThat(new JwtCredentialEnvelopeMatcher(jwt)));
    }

    @Test
    void pushVerifiableCredential_whenDidNotResolved_throws() {
        when(resolverRegistry.resolve(PARTICIPANT_DID)).thenReturn(Result.failure(FAILURE_MESSAGE));

        assertThatCallFailsWith(ERROR_RETRY)
                .isEqualTo(format("Failed to resolve DID %s. %s", PARTICIPANT_DID, FAILURE_MESSAGE));
    }

    @Test
    void pushVerifiableCredential_whenDidDocumentDoesNotContainHubUrl_throws() {
        when(resolverRegistry.resolve(PARTICIPANT_DID))
                .thenReturn(Result.success(DidDocument.Builder.newInstance()
                        .service(List.of(new Service(UUID.randomUUID().toString(), "test-type", IDENTITY_HUB_URL)))
                        .build()));

        assertThatCallFailsWith(FATAL_ERROR)
                .isEqualTo(format("Failed to resolve Identity Hub URL from DID document for %s", PARTICIPANT_DID));
    }

    @Test
    void pushVerifiableCredential_whenJwtCannotBeSigned_throws() throws ParseException, JOSEException {
        when(jwtCredentialFactory.buildSignedJwt(credential, privateKeyWrapper))
                .thenThrow(new JOSEException(FAILURE_MESSAGE));

        assertThatCallFailsWith(FATAL_ERROR)
                .isEqualTo(format("%s: %s", JOSEException.class.getCanonicalName(), FAILURE_MESSAGE));
    }

    @Test
    void pushVerifiableCredential_whenPushToIdentityHubFails_throws() {
        when(identityHubClient.addVerifiableCredential(eq(IDENTITY_HUB_URL), any(CredentialEnvelope.class)))
                .thenReturn(Result.failure(FAILURE_MESSAGE));

        assertThatCallFailsWith(ERROR_RETRY).isEqualTo(format("Failed to send VC. %s", FAILURE_MESSAGE));
    }

    @NotNull
    private AbstractStringAssert<?> assertThatCallFailsWith(ResponseStatus status) {
        StatusResult<Void> result = service.pushVerifiableCredential(participant());
        assertThat(result.failed()).isTrue();
        assertThat(result.getFailure().status()).isEqualTo(status);
        return assertThat(result.getFailureDetail());
    }

    private static Participant participant() {
        return createParticipant().did(PARTICIPANT_DID).build();
    }

    private static final class JwtCredentialEnvelopeMatcher implements ArgumentMatcher<CredentialEnvelope> {

        private final SignedJWT jwt;

        private JwtCredentialEnvelopeMatcher(SignedJWT jwt) {
            this.jwt = jwt;
        }

        @Override
        public boolean matches(CredentialEnvelope argument) {
            if (argument instanceof JwtCredentialEnvelope) {
                var envelope = (JwtCredentialEnvelope) argument;
                return envelope.getJwt().equals(jwt);
            }
            return false;
        }
    }
}
