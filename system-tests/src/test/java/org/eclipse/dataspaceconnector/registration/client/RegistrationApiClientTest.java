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
import org.eclipse.dataspaceconnector.identityhub.credentials.VerifiableCredentialsJwtServiceImpl;
import org.eclipse.dataspaceconnector.identityhub.credentials.model.VerifiableCredential;
import org.eclipse.dataspaceconnector.junit.testfixtures.TestUtils;
import org.eclipse.dataspaceconnector.registration.cli.ClientUtils;
import org.eclipse.dataspaceconnector.registration.cli.CryptoUtils;
import org.eclipse.dataspaceconnector.registration.client.api.RegistryApi;
import org.eclipse.dataspaceconnector.spi.monitor.ConsoleMonitor;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.text.ParseException;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.eclipse.dataspaceconnector.registration.client.RegistrationServiceTestUtils.CLIENT_DID_WEB;
import static org.eclipse.dataspaceconnector.registration.client.RegistrationServiceTestUtils.UNREGISTERED_CLIENT_DID_WEB;

@IntegrationTest
public class RegistrationApiClientTest {
    static final String API_URL = "http://localhost:8182/authority";
    static final Faker FAKER = new Faker();
    static final Monitor MONITOR = new ConsoleMonitor();
    public static final String HUB_BASE_URL = "http://localhost:8181/api/identity-hub";
    static RegistryApi api;
    static IdentityHubClientImpl identityHubClient;

    String participantUrl = FAKER.internet().url();
    Instant startTime = Instant.now();

    @BeforeAll
    static void setUpClass() {
        var apiClient = ClientUtils.createApiClient(API_URL, CLIENT_DID_WEB, TestKeyData.PRIVATE_KEY_P256);
        api = new RegistryApi(apiClient);
        var okHttpClient = new OkHttpClient.Builder().build();
        identityHubClient = new IdentityHubClientImpl(okHttpClient, new ObjectMapper(), MONITOR);
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
    void addsVerifiableCredential() throws Exception {
        var key = Files.readString(new File(TestUtils.findBuildRoot(), "resources/vault/private-key.pem").toPath());
        var authorityPrivateKey = CryptoUtils.parseFromPemEncodedObjects(key);
        var jwtService = new VerifiableCredentialsJwtServiceImpl(new ObjectMapper(), MONITOR);
        var vc = VerifiableCredential.Builder.newInstance()
                .id(UUID.randomUUID().toString())
                .credentialSubject(Map.of("gaiaXMember", "true"))
                .build();
        var jwt = jwtService.buildSignedJwt(vc, "did:web:did-server:test-dataspace-authority", CLIENT_DID_WEB, authorityPrivateKey);
        identityHubClient.addVerifiableCredential(HUB_BASE_URL, jwt);
        // sanity check
        assertThat(getVerifiableCredentialsFromIdentityHub()).noneSatisfy(this::assertIssuedVerifiableCredential);

        api.addParticipant(participantUrl);

        await().atMost(2, MINUTES).untilAsserted(() -> {
            assertThat(getVerifiableCredentialsFromIdentityHub()).anySatisfy(this::assertIssuedVerifiableCredential);
        });
    }

    private Collection<SignedJWT> getVerifiableCredentialsFromIdentityHub() {
        var result = identityHubClient.getVerifiableCredentials(HUB_BASE_URL);
        assertThat(result.succeeded()).isTrue();
        return result.getContent();
    }

    private void assertIssuedVerifiableCredential(SignedJWT jwt) throws ParseException {
        assertThat(jwt.getJWTClaimsSet().getSubject()).isEqualTo(CLIENT_DID_WEB);
        assertThat(jwt.getJWTClaimsSet().getIssueTime()).isAfter(startTime);
    }

    @Test
    void getParticipant() {
        api.addParticipant(participantUrl);

        var response = api.getParticipant();

        assertThat(response.getDid()).isEqualTo(CLIENT_DID_WEB);
        assertThat(response.getUrl()).isEqualTo(participantUrl);
        assertThat(response.getStatus()).isNotNull();
    }

    @Test
    void getParticipant_notFound() {
        //Arrange - Fresh api client with unregistered client DID.
        var apiClient = ClientUtils.createApiClient(API_URL, UNREGISTERED_CLIENT_DID_WEB, TestKeyData.PRIVATE_KEY_P256);
        var api = new RegistryApi(apiClient);

        // look for participant which is not yet registered.
        assertThatThrownBy(api::getParticipant)
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo(404);
    }

}
