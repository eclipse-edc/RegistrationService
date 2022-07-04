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

package org.eclipse.dataspaceconnector.registration.cli;

import com.github.javafaker.Faker;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jwt.SignedJWT;
import org.eclipse.dataspaceconnector.iam.did.crypto.credentials.VerifiableCredentialFactory;
import org.eclipse.dataspaceconnector.iam.did.crypto.key.EcPublicKeyWrapper;
import org.eclipse.dataspaceconnector.registration.client.TestKeyData;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpRequest;

import static org.assertj.core.api.Assertions.assertThat;

class ClientUtilsTest {
    static final Faker FAKER = new Faker();
    static final String AUTHORIZATION = "Authorization";
    static final String BEARER = "Bearer";

    @Test
    void createApiClient() throws Exception {
        var apiUrl = FAKER.internet().url();
        var issuer = FAKER.internet().url();
        var privateKeyData = TestKeyData.PRIVATE_KEY_P256;
        var publicKey = new EcPublicKeyWrapper(JWK.parseFromPEMEncodedObjects(TestKeyData.PUBLIC_KEY_P256).toECKey());

        var requestBuilder = HttpRequest.newBuilder().uri(URI.create(randomUrl()));

        var apiClient = ClientUtils.createApiClient(apiUrl, issuer, privateKeyData);

        apiClient.getRequestInterceptor().accept(requestBuilder);

        var httpHeaders = requestBuilder.build().headers();
        assertThat(httpHeaders.map())
                .containsOnlyKeys(AUTHORIZATION);
        var authorizationHeaders = httpHeaders.allValues(AUTHORIZATION);
        assertThat(authorizationHeaders).hasSize(1);
        var authorizationHeader = authorizationHeaders.get(0);
        var authHeaderParts = authorizationHeader.split(" ", 2);
        assertThat(authHeaderParts[0]).isEqualTo(BEARER);
        var jwt = SignedJWT.parse(authHeaderParts[1]);
        var verificationResult = VerifiableCredentialFactory.verify(jwt, publicKey, apiUrl);
        assertThat(verificationResult.succeeded()).isTrue();
        assertThat(jwt.getJWTClaimsSet().getIssuer()).isEqualTo(issuer);
    }

    static String randomUrl() {
        return "https://" + FAKER.internet().url();
    }
}