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
import org.eclipse.dataspaceconnector.registration.client.models.Participant;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.dataspaceconnector.junit.testfixtures.TestUtils.getFreePort;
import static org.eclipse.dataspaceconnector.registration.client.TestUtils.DATASPACE_DID_WEB;
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

        CommandLine cmd = RegistrationServiceCli.getCommandLine();

        assertThat(getParticipants(cmd, didWeb)).noneSatisfy(p -> assertThat(p.getDid()).isEqualTo(didWeb));

        var addCmdExitCode = cmd.execute(
                "-c", didWeb,
                "-d", DATASPACE_DID_WEB,
                "-k", privateKeyFile.toString(),
                "--http-scheme",
                "participants", "add");
        assertThat(addCmdExitCode).isEqualTo(0);
        assertThat(getParticipants(cmd, didWeb)).anySatisfy(p -> assertThat(p.getDid()).isEqualTo(didWeb));
    }

    private String didDocument() throws JOSEException, JsonProcessingException {
        var publicKey = (ECKey) ECKey.parseFromPEMEncodedObjects(TestKeyData.PUBLIC_KEY_P256);
        var vm = VerificationMethod.Builder.create().id("#my-key-1").type(DidConstants.ECDSA_SECP_256_K_1_VERIFICATION_KEY_2019).controller("")
                .publicKeyJwk(new EllipticCurvePublicKey(publicKey.getCurve().getName(), publicKey.getKeyType().getValue(), publicKey.getX().toString(), publicKey.getY().toString()))
                .build();
        var didDocument = DidDocument.Builder.newInstance().verificationMethod(List.of(vm)).build();
        return MAPPER.writeValueAsString(didDocument);
    }

    private List<Participant> getParticipants(CommandLine cmd, String didWeb) throws JsonProcessingException {
        var writer = new StringWriter();
        cmd.setOut(new PrintWriter(writer));
        var listCmdExitCode = cmd.execute(
                "-c", didWeb,
                "-d", DATASPACE_DID_WEB,
                "-k", privateKeyFile.toString(),
                "--http-scheme",
                "participants", "list");
        assertThat(listCmdExitCode).isEqualTo(0);

        var output = writer.toString();
        return MAPPER.readValue(output, new TypeReference<>() {
        });
    }
}
