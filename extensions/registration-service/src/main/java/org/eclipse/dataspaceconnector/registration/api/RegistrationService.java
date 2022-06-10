package org.eclipse.dataspaceconnector.registration.api;

import org.eclipse.dataspaceconnector.registration.api.model.Participant;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;

import java.util.ArrayList;
import java.util.List;

/**
 * Registration service for dataspace participants.
 */
public class RegistrationService {

    private final Monitor monitor;
    private final InMemoryParticipantStore participantStore;

    public RegistrationService(Monitor monitor, InMemoryParticipantStore participantStore) {
        this.monitor = monitor;
        this.participantStore = participantStore;
    }

    /**
     * Lists all dataspace participants.
     *
     * @return list of dataspace participants.
     */
    public List<Participant> listParticipants() {
        monitor.info("List all participants of the dataspace.");
        return new ArrayList<>(participantStore.listParticipants());
    }

    public void addParticipant(Participant participant) {
        monitor.info("Adding a participant in the dataspace.");
        participantStore.addParticipant(participant);
    }
}
