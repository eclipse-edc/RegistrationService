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

package org.eclipse.edc.registration.cli;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.edc.registration.client.RegistryApiClient;
import org.eclipse.edc.registration.client.TestKeyData;
import org.eclipse.edc.registration.client.model.ParticipantDto;
import org.eclipse.edc.registration.client.response.ApiResult;
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
import static org.eclipse.edc.registration.cli.TestUtils.createParticipantDto;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ParticipantsCommandTest {

    static final ObjectMapper MAPPER = new ObjectMapper();
    static Path privateKeyFile;

    ParticipantDto participant1 = createParticipantDto();
    ParticipantDto participant2 = createParticipantDto();
    String serverUrl = "some.test/url";
    String clientDid = "some.test/url";
    String dataspaceDid = "did:web:" + "test-domain";

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
        app.registryApiClient = mock(RegistryApiClient.class);
        cmd.setOut(new PrintWriter(sw));
    }

    @Test
    void list() throws Exception {
        var participants = List.of(participant1, participant2);
        when(app.registryApiClient.listParticipants())
                .thenReturn(ApiResult.success(participants));

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
                .thenReturn(ApiResult.success(participant1));

        var exitCode = executeGetParticipant();
        assertThat(exitCode).isEqualTo(0);

        var parsedResult = MAPPER.readValue(sw.toString(), ParticipantDto.class);

        assertThat(parsedResult)
                .usingRecursiveComparison()
                .isEqualTo(participant1);
    }

    private void assertAddParticipants(int exitCode, String serverUrl, String service) {
        assertThat(exitCode).isEqualTo(0);
        assertThat(serverUrl).isEqualTo(service);
        verify(app.registryApiClient).addParticipant();
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
