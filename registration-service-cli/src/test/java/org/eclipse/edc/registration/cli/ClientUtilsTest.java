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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jwt.SignedJWT;
import org.eclipse.edc.iam.did.crypto.JwtUtils;
import org.eclipse.edc.iam.did.crypto.key.EcPublicKeyWrapper;
import org.eclipse.edc.registration.client.TestKeyData;
import org.eclipse.edc.registration.client.model.ParticipantDto;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.http.HttpRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.edc.registration.cli.TestUtils.createParticipantDto;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ClientUtilsTest {
    static final ObjectMapper MAPPER = new ObjectMapper();
    static final String AUTHORIZATION = "Authorization";
    static final String BEARER = "Bearer";

    static String randomUrl() {
        return "https://" + "some.test/url";
    }

    @Disabled("Should verify headers upon request against a mockserver")
    @Test
    void createApiClient() throws Exception {
        var apiUrl = "some.test/url";
        var issuer = "some.test/url";
        var privateKeyData = TestKeyData.PRIVATE_KEY_P256;
        var publicKey = new EcPublicKeyWrapper(JWK.parseFromPEMEncodedObjects(TestKeyData.PUBLIC_KEY_P256).toECKey());

        var requestBuilder = HttpRequest.newBuilder().uri(URI.create(randomUrl()));

        var apiClient = ClientUtils.createApiClient(apiUrl, issuer, privateKeyData);

        var httpHeaders = requestBuilder.build().headers();
        assertThat(httpHeaders.map())
                .containsOnlyKeys(AUTHORIZATION);
        var authorizationHeaders = httpHeaders.allValues(AUTHORIZATION);
        assertThat(authorizationHeaders).hasSize(1);
        var authorizationHeader = authorizationHeaders.get(0);
        var authHeaderParts = authorizationHeader.split(" ", 2);
        assertThat(authHeaderParts[0]).isEqualTo(BEARER);
        var jwt = SignedJWT.parse(authHeaderParts[1]);
        var verificationResult = JwtUtils.verify(jwt, publicKey, apiUrl);
        assertThat(verificationResult.succeeded()).isTrue();
        assertThat(jwt.getJWTClaimsSet().getIssuer()).isEqualTo(issuer);
    }

    @Test
    void writeToOutput() throws Exception {
        var commandLine = mock(CommandLine.class);
        var writer = new StringWriter();
        when(commandLine.getOut()).thenReturn(new PrintWriter(writer));
        var participant = createParticipantDto();

        ClientUtils.writeToOutput(commandLine, participant);

        var output = writer.toString();
        var result = MAPPER.readValue(output, ParticipantDto.class);
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(participant);
    }
}
