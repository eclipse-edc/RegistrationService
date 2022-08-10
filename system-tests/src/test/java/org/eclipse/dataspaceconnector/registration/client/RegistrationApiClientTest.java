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
import com.nimbusds.jwt.SignedJWT;
import okhttp3.OkHttpClient;
import org.eclipse.dataspaceconnector.identityhub.client.IdentityHubClientImpl;
import org.eclipse.dataspaceconnector.registration.cli.ClientUtils;
import org.eclipse.dataspaceconnector.registration.client.api.RegistryApi;
import org.eclipse.dataspaceconnector.spi.monitor.ConsoleMonitor;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpStatusCode;

import java.text.ParseException;
import java.time.Instant;
import java.util.Collection;

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.eclipse.dataspaceconnector.junit.testfixtures.TestUtils.getFreePort;
import static org.eclipse.dataspaceconnector.registration.client.TestUtils.didDocument;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.stop.Stop.stopQuietly;

@IntegrationTest
public class RegistrationApiClientTest {
    static final String API_URL = "http://localhost:8182/authority";

    static IdentityHubClientImpl identityHubClient;

    Instant startTime = Instant.now();
    int apiPort;
    ClientAndServer httpSourceClientAndServer;

    @BeforeAll
    static void setUpClass() {
        var okHttpClient = new OkHttpClient.Builder().build();
        identityHubClient = new IdentityHubClientImpl(okHttpClient, new ObjectMapper(), new ConsoleMonitor());
    }

    @BeforeEach
    void setUp() throws Exception {
        apiPort = getFreePort();
        httpSourceClientAndServer = startClientAndServer(apiPort);
        httpSourceClientAndServer.when(request().withPath("/.well-known/did.json"))
                .respond(response()
                        .withBody(didDocument())
                        .withStatusCode(HttpStatusCode.OK_200.code()));
    }

    @AfterEach
    void tearDown() {
        stopQuietly(httpSourceClientAndServer);
    }

    @Test
    void listParticipants() {
        var did = did();
        var api = api(did);

        assertThat(api.listParticipants())
                .noneSatisfy(p -> assertThat(p.getDid()).isEqualTo(did));

        api.addParticipant();

        assertThat(api.listParticipants())
                .anySatisfy(p -> assertThat(p.getDid()).isEqualTo(did));
    }

    @Test
    void addsVerifiableCredential() {
        var did = did();
        var api = api(did);

        // sanity check
        assertThat(getVerifiableCredentialsFromIdentityHub()).noneSatisfy(jwt -> assertIssuedVerifiableCredential(jwt, did));

        api.addParticipant();

        await().atMost(2, MINUTES).untilAsserted(() -> {
            assertThat(getVerifiableCredentialsFromIdentityHub()).anySatisfy(jwt -> assertIssuedVerifiableCredential(jwt, did));
        });
    }

    @Test
    void getParticipant() {
        var did = did();
        var api = api(did);

        api.addParticipant();

        var response = api.getParticipant();

        assertThat(response.getDid()).isEqualTo(did);
    }

    @Test
    void getParticipant_notFound() {

        RegistryApi api = api(did());

        // look for participant which is not yet registered.
        assertThatThrownBy(api::getParticipant)
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo(404);
    }

    private Collection<SignedJWT> getVerifiableCredentialsFromIdentityHub() {
        var result = identityHubClient.getVerifiableCredentials("http://localhost:8181/api/identity-hub");
        assertThat(result.succeeded()).isTrue();
        return result.getContent();
    }

    private void assertIssuedVerifiableCredential(SignedJWT jwt, String clientDid) throws ParseException {
        assertThat(jwt.getJWTClaimsSet().getSubject()).isEqualTo(clientDid);
        assertThat(jwt.getJWTClaimsSet().getIssueTime()).isAfter(startTime);
    }

    @NotNull
    private RegistryApi api(String did) {
        var apiClient = ClientUtils.createApiClient(API_URL, did, TestKeyData.PRIVATE_KEY_P256);
        return new RegistryApi(apiClient);
    }

    @NotNull
    private String did() {
        return "did:web:host.docker.internal%3A" + apiPort;

    }
}
