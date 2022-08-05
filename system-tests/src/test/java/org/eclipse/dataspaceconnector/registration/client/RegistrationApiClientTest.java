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

package org.eclipse.dataspaceconnector.registration.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import com.nimbusds.jwt.SignedJWT;
import okhttp3.OkHttpClient;
import org.eclipse.dataspaceconnector.identityhub.client.IdentityHubClientImpl;
import org.eclipse.dataspaceconnector.registration.cli.ClientUtils;
import org.eclipse.dataspaceconnector.registration.client.api.RegistryApi;
import org.eclipse.dataspaceconnector.spi.monitor.ConsoleMonitor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.time.Instant;
import java.util.Collection;

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.eclipse.dataspaceconnector.registration.client.TestUtils.CLIENT_DID_WEB;

@IntegrationTest
public class RegistrationApiClientTest {
    static final String API_URL = "http://localhost:8182/authority";
    static final Faker FAKER = new Faker();
    static RegistryApi api;
    static IdentityHubClientImpl identityHubClient;

    String participantUrl = FAKER.internet().url();
    Instant startTime = Instant.now();

    @BeforeAll
    static void setUpClass() {
        var apiClient = ClientUtils.createApiClient(API_URL, CLIENT_DID_WEB, TestKeyData.PRIVATE_KEY_P256);
        api = new RegistryApi(apiClient);
        var okHttpClient = new OkHttpClient.Builder().build();
        identityHubClient = new IdentityHubClientImpl(okHttpClient, new ObjectMapper(), new ConsoleMonitor());
    }

    @Test
    void listParticipants() {
        assertThat(api.listParticipants())
                .noneSatisfy(p -> assertThat(p.getUrl()).isEqualTo(participantUrl));

        api.addParticipant(participantUrl);

        assertThat(api.listParticipants())
                .anySatisfy(p -> assertThat(p.getUrl()).isEqualTo(participantUrl));
    }

    @Test
    void addsVerifiableCredential() {
        // sanity check
        assertThat(getVerifiableCredentialsFromIdentityHub()).noneSatisfy(this::assertIssuedVerifiableCredential);

        api.addParticipant(participantUrl);

        await().atMost(2, MINUTES).untilAsserted(() -> {
            assertThat(getVerifiableCredentialsFromIdentityHub()).anySatisfy(this::assertIssuedVerifiableCredential);
        });
    }

    private Collection<SignedJWT> getVerifiableCredentialsFromIdentityHub() {
        var result = identityHubClient.getVerifiableCredentials("http://localhost:8181/api/identity-hub");
        assertThat(result.succeeded()).isTrue();
        return result.getContent();
    }

    private void assertIssuedVerifiableCredential(SignedJWT jwt) throws ParseException {
        assertThat(jwt.getJWTClaimsSet().getSubject()).isEqualTo(CLIENT_DID_WEB);
        assertThat(jwt.getJWTClaimsSet().getIssueTime()).isAfter(startTime);
    }
}
