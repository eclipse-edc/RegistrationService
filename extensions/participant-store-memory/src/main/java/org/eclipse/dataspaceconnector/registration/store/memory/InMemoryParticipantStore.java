package org.eclipse.dataspaceconnector.registration.store.memory;

import org.eclipse.dataspaceconnector.registration.store.model.Participant;
import org.eclipse.dataspaceconnector.registration.store.model.ParticipantStatus;
import org.eclipse.dataspaceconnector.registration.store.spi.ParticipantStore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public void save(Participant participant) {
        storage.put(participant.getName(), participant);
    }

    @Override
    public Collection<Participant> nextForState(ParticipantStatus status, int batchSize) {
        return storage.values().stream().filter(p -> p.getStatus() == status).limit(batchSize).collect(Collectors.toList());
    }
}
