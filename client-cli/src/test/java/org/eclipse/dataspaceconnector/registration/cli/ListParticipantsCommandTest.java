package org.eclipse.dataspaceconnector.registration.cli;

import org.eclipse.dataspaceconnector.registration.client.ApiClient;
import org.eclipse.dataspaceconnector.registration.client.ApiClientFactory;
import org.eclipse.dataspaceconnector.registration.client.api.RegistryApi;
import org.eclipse.dataspaceconnector.registration.client.models.Participant;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ListParticipantsCommandTest {

    static final String API_URL = "http://localhost:8181/api";

    @Test
    void listParticipants() {
        RegistrationServiceCli app = new RegistrationServiceCli();
        app.registryApiClient = mock(RegistryApi.class);
        when(app.registryApiClient.listParticipants())
                .thenReturn(List.of(new Participant()));

        CommandLine cmd = new CommandLine(app);

        StringWriter sw = new StringWriter();
        cmd.setOut(new PrintWriter(sw));

        int exitCode = cmd.execute("-s", "server", "participants", "list");
        assertEquals(0, exitCode);
        assertEquals("Your output is abc...", sw.toString());
    }
}