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

import com.nimbusds.jwt.SignedJWT;
import org.eclipse.dataspaceconnector.registration.client.api.RegistryApi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpStatusCode;

import java.text.ParseException;
import java.time.Instant;
import java.util.Collection;

import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.eclipse.dataspaceconnector.junit.testfixtures.TestUtils.getFreePort;
import static org.eclipse.dataspaceconnector.registration.client.RegistrationServiceTestUtils.HUB_BASE_URL;
import static org.eclipse.dataspaceconnector.registration.client.RegistrationServiceTestUtils.addEnrollmentCredential;
import static org.eclipse.dataspaceconnector.registration.client.RegistrationServiceTestUtils.createApi;
import static org.eclipse.dataspaceconnector.registration.client.RegistrationServiceTestUtils.createDid;
import static org.eclipse.dataspaceconnector.registration.client.RegistrationServiceTestUtils.didDocument;
import static org.eclipse.dataspaceconnector.registration.client.RegistrationServiceTestUtils.identityHubClient;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.stop.Stop.stopQuietly;

@IntegrationTest
class RegistrationApiClientTest {
    static final String API_URL = "http://localhost:8182/authority";

    ClientAndServer httpSourceClientAndServer;
    int apiPort;
    String did;
    RegistryApi api;

    @BeforeEach
    void setUp() throws Exception {
        apiPort = getFreePort();
        did = createDid(apiPort);
        api = createApi(did, API_URL);

        httpSourceClientAndServer = startClientAndServer(apiPort);
        httpSourceClientAndServer.when(request().withPath("/.well-known/did.json"))
                .respond(response()
                        .withBody(didDocument(did))
                        .withStatusCode(HttpStatusCode.OK_200.code()));
    }

    @AfterEach
    void tearDown() {
        stopQuietly(httpSourceClientAndServer);
    }

    @Test
    void listParticipants() throws Exception {
        assertThat(api.listParticipants())
                .noneSatisfy(p -> assertThat(p.getDid()).isEqualTo(did));

        addsVerifiableCredential();

        Thread.sleep(20000);

        assertThat(api.listParticipants())
                .anySatisfy(p -> assertThat(p.getDid()).isEqualTo(did));

    }

    @Test
    void addsVerifiableCredential() throws Exception {
        // jwt claims issue time is set with 1 sec precision, so startTime is set to 1 second before
        var startTime = Instant.now().truncatedTo(SECONDS).minus(1, SECONDS);

        // sanity check
        assertThat(getVerifiableCredentialsFromIdentityHub()).noneSatisfy(token -> assertIssuedVerifiableCredential(token, did, startTime));

        addEnrollmentCredential(did);

        api.addParticipant();

        await().atMost(2, MINUTES).untilAsserted(
                () -> assertThat(getVerifiableCredentialsFromIdentityHub()).anySatisfy(token -> assertIssuedVerifiableCredential(token, did, startTime)));
    }

    private Collection<SignedJWT> getVerifiableCredentialsFromIdentityHub() {
        var result = identityHubClient.getVerifiableCredentials(HUB_BASE_URL);
        assertThat(result.succeeded()).isTrue();
        return result.getContent();
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

    private void assertIssuedVerifiableCredential(SignedJWT jwt, String clientDid, Instant startTime) throws ParseException {
        assertThat(jwt.getJWTClaimsSet().getSubject()).isEqualTo(clientDid);
        assertThat(jwt.getJWTClaimsSet().getIssueTime().toInstant()).isAfter(startTime);
    }

}
