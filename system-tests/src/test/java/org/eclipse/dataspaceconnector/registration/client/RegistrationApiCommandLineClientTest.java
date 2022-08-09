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
import com.github.javafaker.Faker;
import org.eclipse.dataspaceconnector.registration.cli.RegistrationServiceCli;
import org.eclipse.dataspaceconnector.registration.client.models.ParticipantDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.dataspaceconnector.registration.client.TestUtils.CLIENT_DID_WEB;
import static org.eclipse.dataspaceconnector.registration.client.TestUtils.DATASPACE_DID_WEB;
import static org.eclipse.dataspaceconnector.registration.client.TestUtils.UNREGISTERED_CLIENT_DID_WEB;

@IntegrationTest
public class RegistrationApiCommandLineClientTest {
    static final ObjectMapper MAPPER = new ObjectMapper();
    static final Faker FAKER = new Faker();
    static Path privateKeyFile;
    String idsUrl = FAKER.internet().url();

    @BeforeAll
    static void setUpClass() throws Exception {
        privateKeyFile = Files.createTempFile("test", ".pem");
        privateKeyFile.toFile().deleteOnExit();
        Files.writeString(privateKeyFile, TestKeyData.PRIVATE_KEY_P256);
    }

    @Test
    void listParticipants() throws Exception {
        assertThat(listParticipantsCmd()).noneSatisfy(p -> assertThat(p.getUrl()).isEqualTo(idsUrl));

        addParticipantCmd();

        assertThat(listParticipantsCmd()).anySatisfy(p -> assertThat(p.getUrl()).isEqualTo(idsUrl));
    }

    @Test
    void getParticipant() throws Exception {
        addParticipantCmd();

        var result = getParticipantCmd();

        assertThat(result.getDid()).isEqualTo(CLIENT_DID_WEB);
        assertThat(result.getUrl()).isEqualTo(idsUrl);
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

    @Deprecated
    @Test
    void listParticipants_usingServiceUrl() throws Exception {
        CommandLine cmd = RegistrationServiceCli.getCommandLine();
        var writer = new StringWriter();
        cmd.setOut(new PrintWriter(writer));

        // list participant should not find any participant with current idsUrl.
        var listCmdExitCode = cmd.execute(
                "-c", CLIENT_DID_WEB,
                "-k", privateKeyFile.toString(),
                "participants", "list");

        assertThat(listCmdExitCode).isEqualTo(0);
        var output = writer.toString();
        var allParticipants = MAPPER.readValue(output, new TypeReference<List<ParticipantDto>>() {
        });

        assertThat(allParticipants).noneSatisfy(p -> assertThat(p.getUrl()).isEqualTo(idsUrl));

        // Add one participant.
        var addCmdExitCode = cmd.execute(
                "-c", CLIENT_DID_WEB,
                "-k", privateKeyFile.toString(),
                "--http-scheme",
                "participants", "add",
                "--ids-url", idsUrl);
        assertThat(addCmdExitCode).isEqualTo(0);

        // Now at least one participant should exist with current idsUrl.
        writer = new StringWriter();
        cmd.setOut(new PrintWriter(writer));
        listCmdExitCode = cmd.execute(
                "-c", CLIENT_DID_WEB,
                "-k", privateKeyFile.toString(),
                "participants", "list");

        assertThat(listCmdExitCode).isEqualTo(0);
        output = writer.toString();
        allParticipants = MAPPER.readValue(output, new TypeReference<>() {
        });
        assertThat(allParticipants).anySatisfy(p -> assertThat(p.getUrl()).isEqualTo(idsUrl));
    }

    private List<String> commonCmdParams() {

        return List.of(
                "-c", CLIENT_DID_WEB,
                "-d", DATASPACE_DID_WEB,
                "-k", privateKeyFile.toString(),
                "--http-scheme",
                "participants"
        );
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

    private void addParticipantCmd() {
        var addParticipantArgs = new ArrayList<>(commonCmdParams());
        addParticipantArgs.addAll(List.of("add", "--ids-url", idsUrl));
        executeCmd(addParticipantArgs);
    }

    private List<ParticipantDto> listParticipantsCmd() throws JsonProcessingException {
        var listParticipantsArgs = new ArrayList<>(commonCmdParams());
        listParticipantsArgs.add("list");
        var output = executeCmd(listParticipantsArgs);
        return MAPPER.readValue(output, new TypeReference<>() {
        });
    }

    private ParticipantDto getParticipantCmd() throws JsonProcessingException {
        var getParticipantArgs = new ArrayList<>(commonCmdParams());
        getParticipantArgs.add("get");
        var output = executeCmd(getParticipantArgs);

        return MAPPER.readValue(output, ParticipantDto.class);
    }

}
