package org.eclipse.dataspaceconnector.registration.api;

import org.eclipse.dataspaceconnector.registration.api.model.Participant;
import org.eclipse.dataspaceconnector.spi.monitor.ConsoleMonitor;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.types.TypeManager;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RegistrationServiceTest {

    TypeManager typeManager = new TypeManager();
    Monitor monitor = new ConsoleMonitor();

    @Test
    void listParticipants_empty() throws Exception {
        var service = setUpRegistrationService("fakeprefix");
        assertThat(service.listParticipants()).isEmpty();
    }

    @Test
    void listParticipants_fromFiles() throws Exception {
        var service = setUpRegistrationService("test-");
        assertThat(service.listParticipants()).hasSize(3);
        assertThat(service.listParticipants()).extracting(Participant::getName).containsExactlyInAnyOrder("participant1", "participant2", "participant3");
        assertThat(service.listParticipants()).extracting(Participant::getUrl).containsExactlyInAnyOrder("http://localhost:8282", "http://localhost:8283", "http://localhost:8284");
        assertThat(service.listParticipants()).extracting(Participant::getSupportedProtocols).containsOnly(List.of("ids-multipart"));
    }

    @NotNull
    private RegistrationService setUpRegistrationService(String nodeJsonPrefix) throws Exception {
        var sampleFile = getClass().getClassLoader().getResource("test-participant1.json");
        assertThat(sampleFile).isNotNull();
        var nodeJsonDir = Path.of(sampleFile.toURI()).getParent();
        return new RegistrationService(nodeJsonDir, nodeJsonPrefix, typeManager, monitor);
    }
}