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
import org.eclipse.dataspaceconnector.identityhub.credentials.VerifiableCredentialsJwtServiceImpl;
import org.eclipse.dataspaceconnector.identityhub.credentials.model.VerifiableCredential;
import org.eclipse.dataspaceconnector.junit.testfixtures.TestUtils;
import org.eclipse.dataspaceconnector.registration.cli.CryptoUtils;
import org.eclipse.dataspaceconnector.registration.client.api.RegistryApi;
import org.eclipse.dataspaceconnector.spi.monitor.ConsoleMonitor;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpStatusCode;

import java.io.File;
import java.nio.file.Files;
import java.text.ParseException;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.eclipse.dataspaceconnector.junit.testfixtures.TestUtils.getFreePort;
import static org.eclipse.dataspaceconnector.registration.client.RegistrationServiceTestUtils.createApi;
import static org.eclipse.dataspaceconnector.registration.client.RegistrationServiceTestUtils.createDid;
import static org.eclipse.dataspaceconnector.registration.client.RegistrationServiceTestUtils.didDocument;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.stop.Stop.stopQuietly;

@IntegrationTest
public class RegistrationApiClientTest {
    public static final String HUB_BASE_URL = "http://localhost:8181/api/identity-hub";
    static final String API_URL = "http://localhost:8182/authority";
    static final Monitor MONITOR = new ConsoleMonitor();

    static IdentityHubClientImpl identityHubClient;

    ClientAndServer httpSourceClientAndServer;
    int apiPort;
    String did;
    RegistryApi api;

    @BeforeAll
    static void setUpClass() {
        var okHttpClient = new OkHttpClient.Builder().build();
        identityHubClient = new IdentityHubClientImpl(okHttpClient, new ObjectMapper(), MONITOR);
    }

    @BeforeEach
    void setUp() throws Exception {
        apiPort = getFreePort();
        httpSourceClientAndServer = startClientAndServer(apiPort);
        httpSourceClientAndServer.when(request().withPath("/.well-known/did.json"))
                .respond(response()
                        .withBody(didDocument())
                        .withStatusCode(HttpStatusCode.OK_200.code()));
        did = createDid(apiPort);
        api = createApi(did, API_URL);
    }

    @AfterEach
    void tearDown() {
        stopQuietly(httpSourceClientAndServer);
    }

    @Test
    void listParticipants() {
        assertThat(api.listParticipants())
                .noneSatisfy(p -> assertThat(p.getDid()).isEqualTo(did));

        api.addParticipant();

        assertThat(api.listParticipants())
                .anySatisfy(p -> assertThat(p.getDid()).isEqualTo(did));
    }

    @Test
    void addsVerifiableCredential() throws Exception {
        // jwt claims issue time is set with 1 sec precision, so startTime is set to 1 second before
        var startTime = Instant.now().truncatedTo(SECONDS).minus(1, SECONDS);
        var key = Files.readString(new File(TestUtils.findBuildRoot(), "resources/vault/private-key.pem").toPath());
        var authorityPrivateKey = CryptoUtils.parseFromPemEncodedObjects(key);
        var jwtService = new VerifiableCredentialsJwtServiceImpl(new ObjectMapper(), MONITOR);
        var vc = VerifiableCredential.Builder.newInstance()
                .id(UUID.randomUUID().toString())
                .credentialSubject(Map.of("gaiaXMember", "true"))
                .build();

        // sanity check
        assertThat(getVerifiableCredentialsFromIdentityHub()).noneSatisfy(token -> assertIssuedVerifiableCredential(token, did, startTime));

        var jwt = jwtService.buildSignedJwt(vc, "did:web:localhost%3A8080:test-dataspace-authority", did, authorityPrivateKey);
        identityHubClient.addVerifiableCredential(HUB_BASE_URL, jwt);

        api.addParticipant();

        await().atMost(2, MINUTES).untilAsserted(() -> {
            assertThat(getVerifiableCredentialsFromIdentityHub()).anySatisfy(token -> assertIssuedVerifiableCredential(token, did, startTime));
        });
    }

    @Test
    void getParticipant() {
        api.addParticipant();

        var response = api.getParticipant();

        assertThat(response.getDid()).isEqualTo(did);
    }

    @Test
    void getParticipant_notFound() {

        // look for participant which is not yet registered.
        assertThatThrownBy(api::getParticipant)
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo(404);
    }

    private Collection<SignedJWT> getVerifiableCredentialsFromIdentityHub() {
        var result = identityHubClient.getVerifiableCredentials(HUB_BASE_URL);
        assertThat(result.succeeded()).isTrue();
        return result.getContent();
    }

    private void assertIssuedVerifiableCredential(SignedJWT jwt, String clientDid, Instant startTime) throws ParseException {
        assertThat(jwt.getJWTClaimsSet().getSubject()).isEqualTo(clientDid);
        assertThat(jwt.getJWTClaimsSet().getIssueTime().toInstant()).isAfter(startTime);
    }

}
