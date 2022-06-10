package org.eclipse.dataspaceconnector.registration.store.memory;

import org.eclipse.dataspaceconnector.registration.store.model.Participant;
import org.eclipse.dataspaceconnector.registration.store.spi.ParticipantStore;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * In-memory store for dataspace participants.
 */
public class InMemoryParticipantStore implements ParticipantStore {

    private final Map<String, Participant> storage = new LinkedHashMap<>();

    @Override
    public List<Participant> listParticipants() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public void addParticipant(Participant participant) {
        storage.put(participant.getName(), participant);
    }
}
