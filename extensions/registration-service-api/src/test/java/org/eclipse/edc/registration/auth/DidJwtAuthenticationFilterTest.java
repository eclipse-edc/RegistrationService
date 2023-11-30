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

package org.eclipse.edc.registration.auth;

import com.nimbusds.jose.jwk.JWK;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.MultivaluedHashMap;
import org.assertj.core.api.ThrowableAssertAlternative;
import org.eclipse.edc.iam.did.crypto.JwtUtils;
import org.eclipse.edc.iam.did.crypto.key.EcPrivateKeyWrapper;
import org.eclipse.edc.iam.did.crypto.key.EcPublicKeyWrapper;
import org.eclipse.edc.iam.did.spi.resolution.DidPublicKeyResolver;
import org.eclipse.edc.registration.client.TestKeyData;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.result.Result;
import org.eclipse.edc.web.spi.exception.AuthenticationFailedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.eclipse.edc.registration.auth.DidJwtAuthenticationFilter.CALLER_DID_HEADER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DidJwtAuthenticationFilterTest {
    static final String AUTHORIZATION = "Authorization";

    Monitor monitor = mock(Monitor.class);
    DidPublicKeyResolver didPublicKeyResolver = mock(DidPublicKeyResolver.class);
    String audience = "test-audience";
    String issuer = "some.test/url";
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
        when(didPublicKeyResolver.resolvePublicKey(eq(issuer), any()))
                .thenReturn(Result.success(publicKey));

        authHeader = "Bearer " + getTokenFor(audience);
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
            "Bearer"})
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
        when(didPublicKeyResolver.resolvePublicKey(eq(issuer), any()))
                .thenReturn(Result.failure("Test Failure"));

        assertNotAuthenticated("Failed obtaining public key for DID: " + issuer);
    }

    @Test
    void filter_onJwtVerificationFailure_fails() {
        headers.putSingle(AUTHORIZATION, "Bearer " + getTokenFor("other audience test-other-audience"));
        assertNotAuthenticated("Invalid JWT (verification error). Claim verification failed.");
    }

    private String getTokenFor(String targetAudience) {
        return JwtUtils.create(
                privateKey,
                issuer,
                issuer,
                targetAudience,
                Clock.systemUTC()).serialize();
    }

    private ThrowableAssertAlternative<AuthenticationFailedException> assertNotAuthenticated(String message) {
        return assertThatExceptionOfType(AuthenticationFailedException.class)
                .isThrownBy(() -> filter.filter(request))
                .withMessageContaining(message);
    }
}
