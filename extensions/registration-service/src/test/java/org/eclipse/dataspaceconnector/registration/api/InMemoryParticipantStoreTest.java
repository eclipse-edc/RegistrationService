package org.eclipse.dataspaceconnector.registration.api;

import org.eclipse.dataspaceconnector.registration.api.model.Participant;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.dataspaceconnector.registration.api.TestUtils.createParticipant;

class InMemoryParticipantStoreTest {

    InMemoryParticipantStore store = new InMemoryParticipantStore();
    Participant participant1 = createParticipant().build();
    Participant participant1OtherEntry = createParticipant().name(participant1.getName()).build();
    Participant participant2 = createParticipant().build();

    @Test
    void addAndListParticipants() {
        assertThat(store.listParticipants()).isEmpty();

        store.addParticipant(participant1);
        assertThat(store.listParticipants()).containsOnly(participant1);
    }

    @Test
    void addAndListParticipants_removesDuplicates() {
        store.addParticipant(participant1);
        store.addParticipant(participant1OtherEntry);
        assertThat(store.listParticipants()).containsOnly(participant1OtherEntry);
    }

    @Test
    void addAndListParticipants_twoEntries() {
        store.addParticipant(participant1);
        store.addParticipant(participant2);
        assertThat(store.listParticipants()).containsOnly(participant1, participant2);
    }
}