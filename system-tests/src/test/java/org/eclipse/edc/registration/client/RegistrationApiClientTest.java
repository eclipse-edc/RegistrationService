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

package org.eclipse.edc.registration.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import org.assertj.core.api.ThrowingConsumer;
import org.eclipse.edc.identityhub.client.IdentityHubClientImpl;
import org.eclipse.edc.identityhub.credentials.jwt.JwtCredentialEnvelope;
import org.eclipse.edc.identityhub.credentials.jwt.JwtCredentialEnvelopeTransformer;
import org.eclipse.edc.identityhub.spi.credentials.model.CredentialEnvelope;
import org.eclipse.edc.identityhub.spi.credentials.model.VerifiableCredential;
import org.eclipse.edc.identityhub.spi.credentials.transformer.CredentialEnvelopeTransformerRegistryImpl;
import org.eclipse.edc.identityhub.verifier.jwt.VerifiableCredentialsJwtServiceImpl;
import org.eclipse.edc.registration.cli.CryptoUtils;
import org.eclipse.edc.registration.client.api.RegistryApi;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.types.TypeManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpStatusCode;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.eclipse.edc.junit.testfixtures.TestUtils.getFreePort;
import static org.eclipse.edc.registration.client.RegistrationServiceTestUtils.createApi;
import static org.eclipse.edc.registration.client.RegistrationServiceTestUtils.createDid;
import static org.eclipse.edc.registration.client.RegistrationServiceTestUtils.didDocument;
import static org.mockito.Mockito.mock;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.stop.Stop.stopQuietly;

@IntegrationTest
class RegistrationApiClientTest {

    private static final String HUB_BASE_URL = "http://localhost:8181/api/identity-hub";
    private static final String API_URL = "http://localhost:8182/authority";
    private static final Monitor MONITOR = mock(Monitor.class);

    private static IdentityHubClientImpl identityHubClient;

    private ClientAndServer httpSourceClientAndServer;
    private String did;
    private RegistryApi api;

    @BeforeAll
    static void setUpClass() {
        var mapper = new TypeManager().getMapper();
        var okHttpClient = new OkHttpClient.Builder().build();
        var transformerRegistry = new CredentialEnvelopeTransformerRegistryImpl();
        transformerRegistry.register(new JwtCredentialEnvelopeTransformer(mapper));
        identityHubClient = new IdentityHubClientImpl(okHttpClient, mapper, MONITOR, transformerRegistry);
    }

    @BeforeEach
    void setUp() throws Exception {
        var apiPort = getFreePort();
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
        var key = Files.readString(Path.of("resources/vault/private-key.pem"));
        var authorityPrivateKey = CryptoUtils.parseFromPemEncodedObjects(key);
        var jwtService = new VerifiableCredentialsJwtServiceImpl(new ObjectMapper(), MONITOR);
        var vc = VerifiableCredential.Builder.newInstance()
                .id(UUID.randomUUID().toString())
                .credentialSubject(Map.of("gaiaXMember", "true"))
                .build();

        // sanity check
        assertThat(getVerifiableCredentialsFromIdentityHub()).noneSatisfy(vcRequirement(did, startTime));

        var jwt = jwtService.buildSignedJwt(vc, "did:web:localhost%3A8080:test-dataspace-authority", did, authorityPrivateKey);
        identityHubClient.addVerifiableCredential(HUB_BASE_URL, new JwtCredentialEnvelope(jwt));

        api.addParticipant();

        await().atMost(2, MINUTES).untilAsserted(() -> {
            assertThat(getVerifiableCredentialsFromIdentityHub()).anySatisfy(vcRequirement(did, startTime));
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

    private Collection<CredentialEnvelope> getVerifiableCredentialsFromIdentityHub() {
        var result = identityHubClient.getVerifiableCredentials(HUB_BASE_URL);
        assertThat(result.succeeded()).isTrue();
        return result.getContent();
    }

    private ThrowingConsumer<CredentialEnvelope> vcRequirement(String clientDid, Instant startTime) {
        return envelope -> {
            assertThat(envelope).isInstanceOf(JwtCredentialEnvelope.class);
            var jwt = ((JwtCredentialEnvelope) envelope).getJwtVerifiableCredentials();
            assertThat(jwt.getJWTClaimsSet().getSubject()).isEqualTo(clientDid);
            assertThat(jwt.getJWTClaimsSet().getIssueTime().toInstant()).isAfter(startTime);
        };
    }

}
