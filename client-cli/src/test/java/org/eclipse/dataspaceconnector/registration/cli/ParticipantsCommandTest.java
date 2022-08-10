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

package org.eclipse.dataspaceconnector.registration.cli;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import org.eclipse.dataspaceconnector.registration.client.TestKeyData;
import org.eclipse.dataspaceconnector.registration.client.api.RegistryApi;
import org.eclipse.dataspaceconnector.registration.client.models.ParticipantDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.dataspaceconnector.registration.cli.TestUtils.createParticipantDto;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ParticipantsCommandTest {

    static final Faker FAKER = new Faker();
    static final ObjectMapper MAPPER = new ObjectMapper();
    static Path privateKeyFile;

    ParticipantDto participant1 = createParticipantDto();
    ParticipantDto participant2 = createParticipantDto();
    String serverUrl = FAKER.internet().url();
    String clientDid = FAKER.internet().url();
    String dataspaceDid = "did:web:" + FAKER.internet().domainName();

    RegistrationServiceCli app = new RegistrationServiceCli();
    CommandLine cmd = new CommandLine(app);
    StringWriter sw = new StringWriter();

    @BeforeAll
    static void setUpClass() throws Exception {
        privateKeyFile = Files.createTempFile("test", ".pem");
        privateKeyFile.toFile().deleteOnExit();
        Files.writeString(privateKeyFile, TestKeyData.PRIVATE_KEY_P256);
    }

    @BeforeEach
    void setUp() {
        app.registryApiClient = mock(RegistryApi.class);
        cmd.setOut(new PrintWriter(sw));
    }

    @Test
    void list() throws Exception {
        var participants = List.of(this.participant1, participant2);
        when(app.registryApiClient.listParticipants())
                .thenReturn(participants);

        var exitCode = executeParticipantsList("-d", dataspaceDid);
        assertListParticipants(participants, exitCode, app.dataspaceDid, dataspaceDid);
    }

    @Test
    void add() {
        var exitCode = executeParticipantsAdd("-d", dataspaceDid);
        assertAddParticipants(exitCode, dataspaceDid, app.dataspaceDid);
    }

    @Test
    void getParticipant() throws Exception {
        when(app.registryApiClient.getParticipant())
                .thenReturn(participant1);

        var exitCode = executeGetParticipant();
        assertThat(exitCode).isEqualTo(0);

        var parsedResult = MAPPER.readValue(sw.toString(), ParticipantDto.class);

        assertThat(parsedResult)
                .usingRecursiveComparison()
                .isEqualTo(participant1);
    }

    @Deprecated
    @Test
    void list_using_serviceUrl() throws Exception {
        var participants = List.of(this.participant1, participant2);
        when(app.registryApiClient.listParticipants())
                .thenReturn(participants);

        var exitCode = executeParticipantsList("-s", serverUrl);
        assertListParticipants(participants, exitCode, app.service, serverUrl);
    }

    @Deprecated
    @Test
    void add_using_serviceUrl() {
        var exitCode = executeParticipantsAdd("-s", serverUrl);
        assertAddParticipants(exitCode, serverUrl, app.service);
    }

    @Deprecated
    @Test
    void add_both_inputs() {
        var exitCode = cmd.execute(
                "-c", clientDid,
                "-k", privateKeyFile.toString(),
                "-s", serverUrl,
                "-d", dataspaceDid,
                "participants", "add");

        assertThat(exitCode).isEqualTo(0);
        assertThat(dataspaceDid).isEqualTo(app.dataspaceDid);
        assertThat(serverUrl).isEqualTo(app.service);
        verify(app.registryApiClient).addParticipant(app.clientDid);
    }

    private void assertAddParticipants(int exitCode, String serverUrl, String service) {
        assertThat(exitCode).isEqualTo(0);
        assertThat(serverUrl).isEqualTo(service);
        verify(app.registryApiClient).addParticipant(app.clientDid);
    }

    private void assertListParticipants(List<ParticipantDto> participants, int exitCode, String value, String expectedValue) throws JsonProcessingException {
        assertThat(exitCode).isEqualTo(0);
        assertThat(expectedValue).isEqualTo(value);

        var parsedResult = MAPPER.readValue(sw.toString(), new TypeReference<List<ParticipantDto>>() {
        });
        assertThat(parsedResult)
                .usingRecursiveFieldByFieldElementComparator()
                .isEqualTo(participants);
    }

    private int executeParticipantsAdd(String inputCmd, String inputValue) {
        return cmd.execute(
                "-c", clientDid,
                "-k", privateKeyFile.toString(),
                inputCmd, inputValue,
                "participants", "add");
    }

    private int executeParticipantsList(String inputCmd, String inputValue) {
        return cmd.execute(
                "-c", clientDid,
                "-k", privateKeyFile.toString(),
                inputCmd, inputValue,
                "participants", "list");
    }

    private int executeGetParticipant() {
        return cmd.execute(
                "-c", clientDid,
                "-k", privateKeyFile.toString(),
                "-d", dataspaceDid,
                "participants", "get");
    }
}