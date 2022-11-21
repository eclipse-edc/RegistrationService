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

package org.eclipse.edc.registration.cli;

import com.nimbusds.jose.jwk.JWK;
import org.eclipse.edc.iam.did.crypto.key.EcPublicKeyWrapper;
import org.eclipse.edc.registration.client.JsonWebSignatureHeaderInterceptor;
import org.eclipse.edc.registration.client.TestKeyData;
import org.eclipse.edc.spi.iam.TokenRepresentation;
import org.eclipse.edc.spi.result.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpRequest;

import static org.assertj.core.api.Assertions.assertThat;

class JsonWebSignatureHeaderInterceptorTest {

    static final String AUTHORIZATION = "Authorization";
    static final String BEARER = "Bearer";

    String token = "test-token";
    String targetUrl = "some.test/url";
    JsonWebSignatureHeaderInterceptor interceptor;
    EcPublicKeyWrapper publicKey;

    static String randomUrl() {
        return "https://" + "some.test/url";
    }

    @BeforeEach
    void setUp() throws Exception {
        publicKey = new EcPublicKeyWrapper(JWK.parseFromPEMEncodedObjects(TestKeyData.PUBLIC_KEY_P256).toECKey());
        interceptor = new JsonWebSignatureHeaderInterceptor(parameters -> Result.success(TokenRepresentation.Builder.newInstance().token(token).build()), targetUrl);
    }

    @Test
    void accept() {
        var requestBuilder = HttpRequest.newBuilder().uri(URI.create(randomUrl()));

        interceptor.accept(requestBuilder);

        var httpHeaders = requestBuilder.build().headers();
        assertThat(httpHeaders.map())
                .containsOnlyKeys(AUTHORIZATION);
        var authorizationHeaders = httpHeaders.allValues(AUTHORIZATION);
        assertThat(authorizationHeaders).hasSize(1);
        var authorizationHeader = authorizationHeaders.get(0);
        var authHeaderParts = authorizationHeader.split(" ", 2);
        assertThat(authHeaderParts[0]).isEqualTo(BEARER);
        assertThat(authHeaderParts[1]).isEqualTo(token);
    }
}
