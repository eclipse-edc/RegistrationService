package org.eclipse.dataspaceconnector.registration;

import com.github.javafaker.Faker;
import org.eclipse.dataspaceconnector.registration.store.model.Participant;
import org.eclipse.dataspaceconnector.registration.store.model.ParticipantStatus;

public class TestUtils {
    private TestUtils() {
    }

    static final Faker FAKER = new Faker();

    public static Participant.Builder createParticipant() {
        return Participant.Builder.newInstance()
                .status(FAKER.options().option(ParticipantStatus.class))
                .name(FAKER.lorem().characters())
                .url(FAKER.internet().url())
                .supportedProtocol(FAKER.lorem().word())
                .supportedProtocol(FAKER.lorem().word());
    }
}