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

package org.eclipse.dataspaceconnector.registration.auth;

import com.github.javafaker.Faker;
import com.nimbusds.jose.jwk.JWK;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.MultivaluedHashMap;
import org.assertj.core.api.ThrowableAssertAlternative;
import org.eclipse.dataspaceconnector.iam.did.crypto.credentials.VerifiableCredentialFactory;
import org.eclipse.dataspaceconnector.iam.did.crypto.key.EcPrivateKeyWrapper;
import org.eclipse.dataspaceconnector.iam.did.crypto.key.EcPublicKeyWrapper;
import org.eclipse.dataspaceconnector.iam.did.spi.resolution.DidPublicKeyResolver;
import org.eclipse.dataspaceconnector.registration.client.TestKeyData;
import org.eclipse.dataspaceconnector.spi.exception.AuthenticationFailedException;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.result.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.eclipse.dataspaceconnector.registration.auth.DidJwtAuthenticationFilter.CALLER_DID_HEADER;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DidJwtAuthenticationFilterTest {
    static final Faker FAKER = new Faker();
    static final String AUTHORIZATION = "Authorization";

    Monitor monitor = mock(Monitor.class);
    DidPublicKeyResolver didPublicKeyResolver = mock(DidPublicKeyResolver.class);
    String audience = FAKER.lorem().sentence();
    String issuer = FAKER.internet().url();
    DidJwtAuthenticationFilter filter = new DidJwtAuthenticationFilter(monitor, didPublicKeyResolver, audience);

    ContainerRequestContext request = mock(ContainerRequestContext.class);
    MultivaluedHashMap<String, String> headers = new MultivaluedHashMap<>();
    EcPrivateKeyWrapper privateKey;

    private String authHeader;

    @BeforeEach
    void setUp() throws Exception {
        when(request.getHeaders()).thenReturn(headers);
        privateKey = new EcPrivateKeyWrapper(JWK.parseFromPEMEncodedObjects(TestKeyData.PRIVATE_KEY_P256).toECKey());
        var publicKey = new EcPublicKeyWrapper(JWK.parseFromPEMEncodedObjects(TestKeyData.PUBLIC_KEY_P256).toECKey());
        when(didPublicKeyResolver.resolvePublicKey(issuer))
                .thenReturn(Result.success(publicKey));

        authHeader = "Bearer " + getTokenFor(audience);
    }

    private String getTokenFor(String targetAudience) {
        return VerifiableCredentialFactory.create(
                privateKey,
                issuer,
                targetAudience,
                Clock.systemUTC()).serialize();
    }

    @Test
    void filter_success() {
        headers.putSingle(AUTHORIZATION, authHeader);
        filter.filter(request);

        assertThat(headers)
                .containsOnlyKeys(AUTHORIZATION, CALLER_DID_HEADER);
        assertThat(headers.get(AUTHORIZATION)).containsExactly(authHeader); // assert not modified
        assertThat(headers.get(CALLER_DID_HEADER)).containsExactly(issuer);
    }

    @Test
    void filter_onMissingAuthHeader_fails() {
        assertNotAuthenticated("Cannot authenticate request. Missing Authorization header");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            " ",
            "Bear ey",
            "Bearer ey f",
            "Bearer" })
    void filter_onInvalidAuthHeader_fails(String header) {
        headers.putSingle(AUTHORIZATION, header);
        assertNotAuthenticated("Cannot authenticate request. Authorization header value is not a valid Bearer token");
    }

    @Test
    void filter_onInvalidJwt_fails() {
        headers.putSingle(AUTHORIZATION, "Bearer " + "x" + getTokenFor(audience));
        assertNotAuthenticated("Invalid JWT (parse error)");
    }

    @Test
    void filter_onUnresolvedDid_fails() {
        headers.putSingle(AUTHORIZATION, authHeader);
        when(didPublicKeyResolver.resolvePublicKey(issuer))
                .thenReturn(Result.failure(FAKER.lorem().sentence()));

        assertNotAuthenticated("Failed obtaining public key for DID: " + issuer);
    }

    @Test
    void filter_onJwtVerificationFailure_fails() {
        headers.putSingle(AUTHORIZATION, "Bearer " + getTokenFor("other audience " + FAKER.lorem().word()));
        assertNotAuthenticated("Invalid JWT (verification error). Claim verification failed.");
    }

    private ThrowableAssertAlternative<AuthenticationFailedException> assertNotAuthenticated(String message) {
        return assertThatExceptionOfType(AuthenticationFailedException.class)
                .isThrownBy(() -> filter.filter(request))
                .withMessageContaining(message);
    }
}