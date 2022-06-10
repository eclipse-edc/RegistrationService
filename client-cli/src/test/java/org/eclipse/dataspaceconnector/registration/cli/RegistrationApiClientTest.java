package org.eclipse.dataspaceconnector.registration.cli;

import org.eclipse.dataspaceconnector.registration.client.ApiClient;
import org.eclipse.dataspaceconnector.registration.client.ApiClientFactory;
import org.eclipse.dataspaceconnector.registration.client.api.RegistryApi;
import org.eclipse.dataspaceconnector.registration.client.models.Participant;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class RegistrationApiClientTest {

    static final String API_URL = "http://localhost:8181/api";

    ApiClient apiClient = ApiClientFactory.createApiClient(API_URL);
    RegistryApi api = new RegistryApi(apiClient);

    @Test
    void listParticipants() {
        RegistrationServiceCli app = new RegistrationServiceCli();
        CommandLine cmd = new CommandLine(app);

        StringWriter sw = new StringWriter();
        cmd.setOut(new PrintWriter(sw));

// black box testing
        int exitCode = cmd.execute("-x", "-y=123");
        assertEquals(0, exitCode);
        assertEquals("Your output is abc...", sw.toString());

// white box testing
        assertEquals("expectedValue1", app.getState1());
        assertEquals("expectedValue2", app.getState2());
    }
}
