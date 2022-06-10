package org.eclipse.dataspaceconnector.registration.store.spi;

import org.eclipse.dataspaceconnector.registration.store.model.Participant;

import java.util.List;

public interface ParticipantStore {
    List<Participant> listParticipants();

    void addParticipant(Participant participant);
}
