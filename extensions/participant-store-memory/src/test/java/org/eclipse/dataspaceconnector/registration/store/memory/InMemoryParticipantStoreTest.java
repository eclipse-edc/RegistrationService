package org.eclipse.dataspaceconnector.registration.store.memory;

import org.eclipse.dataspaceconnector.registration.TestUtils;
import org.eclipse.dataspaceconnector.registration.store.model.Participant;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.dataspaceconnector.registration.store.model.ParticipantStatus.AUTHORIZED;
import static org.eclipse.dataspaceconnector.registration.store.model.ParticipantStatus.ONBOARDING_INITIATED;

class InMemoryParticipantStoreTest {

    InMemoryParticipantStore store = new InMemoryParticipantStore();
    Participant participant1 = TestUtils.createParticipant().build();
    Participant participant1OtherEntry = TestUtils.createParticipant().name(participant1.getName()).build();
    Participant participant2 = TestUtils.createParticipant().build();

    @Test
    void saveAndListParticipants() {
        assertThat(store.listParticipants()).isEmpty();

        store.save(participant1);
        assertThat(store.listParticipants()).containsOnly(participant1);
    }

    @Test
    void saveAndListParticipants_removesDuplicates() {
        store.save(participant1);
        store.save(participant1OtherEntry);
        assertThat(store.listParticipants()).containsOnly(participant1OtherEntry);
    }

    @Test
    void saveAndListParticipants_twoEntries() {
        store.save(participant1);
        store.save(participant2);
        assertThat(store.listParticipants()).containsOnly(participant1, participant2);
    }

    @Test
    void nextForState() {
        IntStream.range(0, 2).forEach(i -> store.save(TestUtils.createParticipant().status(ONBOARDING_INITIATED).build()));
        IntStream.range(0, 15).forEach(i -> store.save(TestUtils.createParticipant().status(AUTHORIZED).build()));
        IntStream.range(0, 2).forEach(i -> store.save(TestUtils.createParticipant().status(ONBOARDING_INITIATED).build()));
        assertThat(store.nextForState(ONBOARDING_INITIATED, 3))
                .hasSize(3)
                .allMatch(p -> p.getStatus() == ONBOARDING_INITIATED);
    }
}