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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import org.eclipse.dataspaceconnector.identityhub.client.IdentityHubClientImpl;
import org.eclipse.dataspaceconnector.identityhub.credentials.VerifiableCredentialsJwtServiceImpl;
import org.eclipse.dataspaceconnector.identityhub.credentials.model.VerifiableCredential;
import org.eclipse.dataspaceconnector.junit.testfixtures.TestUtils;
import org.eclipse.dataspaceconnector.registration.cli.CryptoUtils;
import org.eclipse.dataspaceconnector.registration.cli.RegistrationServiceCli;
import org.eclipse.dataspaceconnector.registration.client.models.ParticipantDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpStatusCode;
import picocli.CommandLine;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.dataspaceconnector.junit.testfixtures.TestUtils.getFreePort;
import static org.eclipse.dataspaceconnector.registration.client.RegistrationApiClientTest.HUB_BASE_URL;
import static org.eclipse.dataspaceconnector.registration.client.RegistrationApiClientTest.MONITOR;
import static org.eclipse.dataspaceconnector.registration.client.RegistrationServiceTestUtils.DATASPACE_DID_WEB;
import static org.eclipse.dataspaceconnector.registration.client.RegistrationServiceTestUtils.DATASPACE_DID_WEB2;
import static org.eclipse.dataspaceconnector.registration.client.RegistrationServiceTestUtils.createDid;
import static org.eclipse.dataspaceconnector.registration.client.RegistrationServiceTestUtils.didDocument;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.stop.Stop.stopQuietly;

@IntegrationTest
public class RegistrationApiCommandLineClientTest {

    static final ObjectMapper MAPPER = new ObjectMapper();
    static Path privateKeyFile;

    int apiPort;
    String did;
    /*
    host.docker.internal is used in docker-compose file to connect from Registration Service container to a mock-service on the host
     */
    ClientAndServer httpSourceClientAndServer;
    private IdentityHubClientImpl identityHubClient;

    @BeforeEach
    void setUpClass() throws Exception {
        privateKeyFile = Files.createTempFile("test", ".pem");
        privateKeyFile.toFile().deleteOnExit();
        Files.writeString(privateKeyFile, TestKeyData.PRIVATE_KEY_P256);
        apiPort = getFreePort();
        did = createDid(apiPort);
        httpSourceClientAndServer = startClientAndServer(apiPort);
        httpSourceClientAndServer.when(request().withPath("/.well-known/did.json"))
                .respond(response()
                        .withBody(didDocument(did))
                        .withStatusCode(HttpStatusCode.OK_200.code()));
        var okHttpClient = new OkHttpClient.Builder().build();
        identityHubClient = new IdentityHubClientImpl(okHttpClient, new ObjectMapper(), MONITOR);
    }

    @AfterEach
    void tearDown() {
        stopQuietly(httpSourceClientAndServer);
    }

    @Test
    void listParticipants() throws Exception {

        assertThat(listParticipantCmd(did)).noneSatisfy(p -> assertThat(p.getDid()).isEqualTo(did));

        var key = Files.readString(new File(TestUtils.findBuildRoot(), "resources/vault/private-key.pem").toPath());
        var authorityPrivateKey = CryptoUtils.parseFromPemEncodedObjects(key);
        var jwtService = new VerifiableCredentialsJwtServiceImpl(new ObjectMapper(), MONITOR);
        var vc = VerifiableCredential.Builder.newInstance()
                .id(UUID.randomUUID().toString())
                .credentialSubject(Map.of("gaiaXMember", "true"))
                .build();

        var jwt = jwtService.buildSignedJwt(vc, DATASPACE_DID_WEB, did, authorityPrivateKey);
        identityHubClient.addVerifiableCredential(HUB_BASE_URL, jwt);

        addParticipantCmd(did);

        Thread.sleep(10000);

        assertThat(listParticipantCmd(did)).anySatisfy(p -> assertThat(p.getDid()).isEqualTo(did));
    }

    @Test
    void getParticipant() throws Exception {

        addParticipantCmd(did);

        var result = getParticipantCmd(did);

        assertThat(result.getDid()).isEqualTo(did);
        assertThat(result.getStatus()).isNotNull();
    }

    @Test
    void getParticipant_notFound() {
        CommandLine cmd = RegistrationServiceCli.getCommandLine();
        var writer = new StringWriter();
        cmd.setOut(new PrintWriter(writer));

        var statusCmdExitCode = cmd.execute(
                "-c", did,
                "-d", DATASPACE_DID_WEB2,
                "-k", privateKeyFile.toString(),
                "--http-scheme",
                "participants", "get");

        assertThat(statusCmdExitCode).isEqualTo(1);
        var output = writer.toString();
        assertThat(output).isEmpty();
    }

    private String executeCmd(List<String> cmdArgs) {
        CommandLine cmd = RegistrationServiceCli.getCommandLine();
        var writer = new StringWriter();
        cmd.setOut(new PrintWriter(writer));

        var cmdExitCode = cmd.execute(cmdArgs.toArray(new String[0]));
        var output = writer.toString();

        assertThat(cmdExitCode).isEqualTo(0);

        return output;
    }

    private List<String> commonCmdParams(String didWeb) {

        return List.of(
                "-c", didWeb,
                "-d", DATASPACE_DID_WEB2,
                "-k", privateKeyFile.toString(),
                "--http-scheme",
                "participants"
        );
    }

    private ParticipantDto getParticipantCmd(String didWeb) throws JsonProcessingException {
        var getParticipantArgs = new ArrayList<>(commonCmdParams(didWeb));
        getParticipantArgs.add("get");
        var output = executeCmd(getParticipantArgs);

        return MAPPER.readValue(output, ParticipantDto.class);
    }

    private void addParticipantCmd(String clientDidWeb) {
        var getParticipantArgs = new ArrayList<>(commonCmdParams(clientDidWeb));
        getParticipantArgs.add("add");
        executeCmd(getParticipantArgs);
    }

    private List<ParticipantDto> listParticipantCmd(String didWeb) throws JsonProcessingException {
        var getParticipantArgs = new ArrayList<>(commonCmdParams(didWeb));
        getParticipantArgs.add("list");
        var output = executeCmd(getParticipantArgs);

        return MAPPER.readValue(output, new TypeReference<>() {
        });
    }
}
