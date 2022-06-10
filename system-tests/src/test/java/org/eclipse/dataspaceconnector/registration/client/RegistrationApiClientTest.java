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

import org.eclipse.dataspaceconnector.registration.cli.RegistrationServiceCli;
import org.eclipse.dataspaceconnector.registration.client.api.RegistryApi;
import org.eclipse.dataspaceconnector.registration.client.models.Participant;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.dataspaceconnector.registration.client.IntegrationTestUtils.createParticipant;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.eclipse.dataspaceconnector.registration.client.IntegrationTestUtils.createParticipant;

@IntegrationTest
public class RegistrationApiClientTest {
    static final String API_URL = "http://localhost:8181/api";

    ApiClient apiClient = ApiClientFactory.createApiClient(API_URL);
    RegistryApi api = new RegistryApi(apiClient);
    Participant participant = createParticipant();

    @Test
    void listParticipants() {
        assertThat(api.listParticipants())
                .doesNotContain(participant);

        api.addParticipant(participant);

        assertThat(api.listParticipants())
                .contains(participant);
    }

    @Test
    void listParticipantsFromCLIClient() {
        RegistrationServiceCli app = new RegistrationServiceCli();
        CommandLine cmd = new CommandLine(app);

        var strings = new ArrayList<String>();
        strings.add("participants");
        strings.add("list");
        int exitCode = cmd.execute(strings.toArray(String[]::new));
        assertEquals(0, exitCode);

        assertThat(api.listParticipants())
                .containsExactly(participant);
    }
}
