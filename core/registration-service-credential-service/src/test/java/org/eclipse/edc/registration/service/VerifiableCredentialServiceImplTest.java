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
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import org.assertj.core.api.AbstractStringAssert;
import org.eclipse.edc.iam.did.crypto.key.EcPrivateKeyWrapper;
import org.eclipse.edc.iam.did.spi.document.DidDocument;
import org.eclipse.edc.iam.did.spi.document.Service;
import org.eclipse.edc.iam.did.spi.key.PrivateKeyWrapper;
import org.eclipse.edc.iam.did.spi.resolution.DidResolverRegistry;
import org.eclipse.edc.identityhub.client.spi.IdentityHubClient;
import org.eclipse.edc.identityhub.credentials.jwt.JwtCredentialEnvelope;
import org.eclipse.edc.identityhub.spi.credentials.model.Credential;
import org.eclipse.edc.identityhub.spi.credentials.model.CredentialEnvelope;
import org.eclipse.edc.identityhub.spi.credentials.model.CredentialSubject;
import org.eclipse.edc.registration.spi.model.Participant;
import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.response.ResponseStatus;
import org.eclipse.edc.spi.response.StatusResult;
import org.eclipse.edc.spi.result.Result;
import org.eclipse.edc.spi.types.TypeManager;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.edc.registration.ParticipantUtils.createParticipant;
import static org.eclipse.edc.spi.response.ResponseStatus.ERROR_RETRY;
import static org.eclipse.edc.spi.response.ResponseStatus.FATAL_ERROR;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
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
    private final Credential credential = Credential.Builder.newInstance()
            .id("id")
            .context("context")
            .issuer(DATASPACE_DID)
            .type("type")
            .credentialSubject(CredentialSubject.Builder.newInstance()
                    .id("test")
                    .claim("foo", "bar")
                    .build())
            .issuanceDate(Date.from(Instant.now().truncatedTo(ChronoUnit.SECONDS)))
            .build();

    private VerifiableCredentialServiceImpl service;

    @BeforeEach
    void setUp() {
        when(resolverRegistry.resolve(PARTICIPANT_DID))
                .thenReturn(Result.success(DidDocument.Builder.newInstance()
                        .service(List.of(new Service(UUID.randomUUID().toString(), IDENTITY_HUB_TYPE, IDENTITY_HUB_URL)))
                        .build()));

        var keypair = generateKeyPair();
        var wrapper = new EcPrivateKeyWrapper(keypair);

        service = new VerifiableCredentialServiceImpl(monitor, wrapper, DATASPACE_DID, resolverRegistry, identityHubClient, p -> Result.success(credential), MAPPER);
    }

    @Test
    void pushVerifiableCredential_createsMembershipCredential() {
        var captor = ArgumentCaptor.forClass(CredentialEnvelope.class);
        when(identityHubClient.addVerifiableCredential(eq(IDENTITY_HUB_URL), captor.capture()))
                .thenReturn(StatusResult.success());

        var result = service.pushVerifiableCredential(participant());

        assertThat(result.succeeded()).isTrue();
        var captured = captor.getValue();
        assertThat(captured).isNotNull().isInstanceOf(JwtCredentialEnvelope.class);
        var jwtEnvelope = (JwtCredentialEnvelope) captured;
        var vcResult = jwtEnvelope.toVerifiableCredential(MAPPER);
        assertThat(vcResult.succeeded()).isTrue();
        assertThat(vcResult.getContent().getItem()).usingRecursiveComparison().isEqualTo(credential);
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
    void pushVerifiableCredential_whenJwtCannotBeSigned_throws() {
        service = new VerifiableCredentialServiceImpl(monitor, mock(PrivateKeyWrapper.class), DATASPACE_DID, resolverRegistry, identityHubClient, p -> Result.success(credential), MAPPER);

        assertThatCallFailsWith(FATAL_ERROR).isEqualTo(NullPointerException.class.getCanonicalName());
    }

    @Test
    void pushVerifiableCredential_whenPushToIdentityHubFails_throws() {
        when(identityHubClient.addVerifiableCredential(eq(IDENTITY_HUB_URL), any(CredentialEnvelope.class)))
                .thenReturn(StatusResult.failure(FATAL_ERROR, FAILURE_MESSAGE));

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

    private static ECKey generateKeyPair() {
        try {
            return new ECKeyGenerator(Curve.P_256)
                    .keyUse(KeyUse.SIGNATURE) // indicate the intended use of the key
                    .keyID(UUID.randomUUID().toString()) // give the key a unique ID
                    .generate();
        } catch (JOSEException e) {
            throw new EdcException(e);
        }
    }
}
