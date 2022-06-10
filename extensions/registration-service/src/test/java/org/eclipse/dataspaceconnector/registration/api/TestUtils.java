package org.eclipse.dataspaceconnector.registration.api;

import com.github.javafaker.Faker;
import org.eclipse.dataspaceconnector.registration.api.model.Participant;

public class TestUtils {
    private TestUtils() {
    }

    static final Faker FAKER = new Faker();

    public static Participant.Builder createParticipant() {
        return Participant.Builder.newInstance()
                .name(FAKER.lorem().characters())
                .url(FAKER.internet().url())
                .supportedProtocol(FAKER.lorem().word())
                .supportedProtocol(FAKER.lorem().word());
    }
}