package org.eclipse.dataspaceconnector.registration.api;

import org.eclipse.dataspaceconnector.registration.api.model.Participant;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * In-memory store for dataspace participants.
 */
public class InMemoryParticipantStore {

    private final Map<String, Participant> participantStore = new LinkedHashMap<>();

    public List<Participant> listParticipants() {
        return new ArrayList<>(participantStore.values());
    }

    public void addParticipant(Participant participant) {
        participantStore.put(participant.getName(), participant);
    }
}
