package org.eclipse.dataspaceconnector.registration.store.memory;

import org.eclipse.dataspaceconnector.registration.store.spi.ParticipantStore;
import org.eclipse.dataspaceconnector.spi.system.Provider;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension;

/**
 * EDC extension to boot the in memory store.
 */
public class InMemoryParticipantStoreExtension implements ServiceExtension {

    @Provider
    public ParticipantStore participantStore() {
        return new InMemoryParticipantStore();
    }
}
