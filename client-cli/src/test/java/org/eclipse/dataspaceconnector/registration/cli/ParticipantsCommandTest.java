package org.eclipse.dataspaceconnector.registration.cli;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import org.eclipse.dataspaceconnector.registration.client.api.RegistryApi;
import org.eclipse.dataspaceconnector.registration.client.models.Participant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.dataspaceconnector.registration.cli.TestUtils.createParticipant;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ParticipantsCommandTest {

    static final Faker FAKER = new Faker();
    static final ObjectMapper MAPPER = new ObjectMapper();

    Participant participant1 = createParticipant();
    Participant participant2 = createParticipant();
    String serverUrl = FAKER.internet().url();

    RegistrationServiceCli app = new RegistrationServiceCli();
    CommandLine cmd = new CommandLine(app);
    StringWriter sw = new StringWriter();

    @BeforeEach
    void setUp() {
        app.registryApiClient = mock(RegistryApi.class);
        cmd.setOut(new PrintWriter(sw));
    }

    @Test
    void list() throws Exception {
        List<Participant> participants = List.of(this.participant1, participant2);
        when(app.registryApiClient.listParticipants())
                .thenReturn(participants);

        int exitCode = cmd.execute("-s", serverUrl, "participants", "list");
        assertEquals(0, exitCode);
        assertEquals(app.service, serverUrl);

        var parsedResult = MAPPER.readValue(sw.toString(), new TypeReference<List<Participant>>() {
        });
        assertThat(parsedResult)
                .usingRecursiveFieldByFieldElementComparator()
                .isEqualTo(participants);
    }
}