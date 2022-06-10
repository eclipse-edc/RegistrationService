package org.eclipse.dataspaceconnector.registration.store.spi;

import org.eclipse.dataspaceconnector.registration.store.model.Participant;
import org.eclipse.dataspaceconnector.registration.store.model.ParticipantStatus;

import java.util.Collection;
import java.util.List;

public interface ParticipantStore {
    List<Participant> listParticipants();

    void save(Participant participant);

    Collection<Participant> nextForState(ParticipantStatus state, int batchSize);
}
