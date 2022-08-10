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
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.ECKey;
import org.eclipse.dataspaceconnector.iam.did.spi.document.DidConstants;
import org.eclipse.dataspaceconnector.iam.did.spi.document.DidDocument;
import org.eclipse.dataspaceconnector.iam.did.spi.document.EllipticCurvePublicKey;
import org.eclipse.dataspaceconnector.iam.did.spi.document.VerificationMethod;
import org.eclipse.dataspaceconnector.registration.cli.RegistrationServiceCli;
import org.eclipse.dataspaceconnector.registration.client.models.ParticipantDto;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpStatusCode;
import picocli.CommandLine;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.dataspaceconnector.junit.testfixtures.TestUtils.getFreePort;
import static org.eclipse.dataspaceconnector.registration.client.TestUtils.DATASPACE_DID_WEB;
import static org.eclipse.dataspaceconnector.registration.client.TestUtils.UNREGISTERED_CLIENT_DID_WEB;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.stop.Stop.stopQuietly;

@IntegrationTest
public class RegistrationApiCommandLineClientTest {
    private static final int API_PORT = getFreePort();

    static final ObjectMapper MAPPER = new ObjectMapper();
    static Path privateKeyFile;
    /*
    host.docker.internal is used in docker-compose file to connect from Registration Service container to a mock-service on the host
     */
    String didWeb = "did:web:host.docker.internal%3A" + API_PORT;
    static ClientAndServer httpSourceClientAndServer;

    @BeforeAll
    static void setUpClass() throws Exception {
        privateKeyFile = Files.createTempFile("test", ".pem");
        privateKeyFile.toFile().deleteOnExit();
        Files.writeString(privateKeyFile, TestKeyData.PRIVATE_KEY_P256);
        httpSourceClientAndServer = startClientAndServer(API_PORT);
    }

    @AfterAll
    public static void tearDown() {
        stopQuietly(httpSourceClientAndServer);
    }

    @Test
    void listParticipants() throws Exception {

        httpSourceClientAndServer.when(request().withPath("/.well-known/did.json"))
                .respond(response()
                        .withBody(didDocument())
                        .withStatusCode(HttpStatusCode.OK_200.code()));

        assertThat(listParticipantCmd(didWeb)).noneSatisfy(p -> assertThat(p.getDid()).isEqualTo(didWeb));

        addParticipantCmd(didWeb);

        assertThat(listParticipantCmd(didWeb)).anySatisfy(p -> assertThat(p.getDid()).isEqualTo(didWeb));
    }

    @Test
    void getParticipant() throws Exception {
        httpSourceClientAndServer.when(request().withPath("/.well-known/did.json"))
                .respond(response()
                        .withBody(didDocument())
                        .withStatusCode(HttpStatusCode.OK_200.code()));

        addParticipantCmd(didWeb);

        var result = getParticipantCmd(didWeb);

        assertThat(result.getDid()).isEqualTo(didWeb);
        assertThat(result.getStatus()).isNotNull();
    }

    @Test
    void getParticipant_notFound() {
        CommandLine cmd = RegistrationServiceCli.getCommandLine();
        var writer = new StringWriter();
        cmd.setOut(new PrintWriter(writer));

        var statusCmdExitCode = cmd.execute(
                "-c", UNREGISTERED_CLIENT_DID_WEB,
                "-d", DATASPACE_DID_WEB,
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
                "-d", DATASPACE_DID_WEB,
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

    private List<ParticipantDto> listParticipantCmd(String clientDidWeb) throws JsonProcessingException {
        var getParticipantArgs = new ArrayList<>(commonCmdParams(clientDidWeb));
        getParticipantArgs.add("list");
        var output = executeCmd(getParticipantArgs);

        return MAPPER.readValue(output, new TypeReference<>() {
        });
    }

    private String didDocument() throws JOSEException, JsonProcessingException {
        var publicKey = (ECKey) ECKey.parseFromPEMEncodedObjects(TestKeyData.PUBLIC_KEY_P256);
        var vm = VerificationMethod.Builder.create().id("#my-key-1").type(DidConstants.ECDSA_SECP_256_K_1_VERIFICATION_KEY_2019).controller("")
                .publicKeyJwk(new EllipticCurvePublicKey(publicKey.getCurve().getName(), publicKey.getKeyType().getValue(), publicKey.getX().toString(), publicKey.getY().toString()))
                .build();
        var didDocument = DidDocument.Builder.newInstance().verificationMethod(List.of(vm)).build();
        return MAPPER.writeValueAsString(didDocument);
    }

}
