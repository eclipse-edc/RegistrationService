package org.eclipse.dataspaceconnector.registration.client;

import org.eclipse.dataspaceconnector.registration.client.api.RegistryApi;
import org.eclipse.dataspaceconnector.registration.client.models.Participant;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.dataspaceconnector.registration.client.IntegrationTestUtils.createParticipant;

@IntegrationTest
public class RegistrationApiClientTest {
    static final String API_URL = "http://localhost:8181/api";

    ApiClient apiClient = ApiClientFactory.createApiClient(API_URL);
    RegistryApi api = new RegistryApi(apiClient);
    Participant participant = createParticipant();

    @Test
    void listParticipants() {
        assertThat(api.listParticipants())
                .doesNotContain(participant);

        api.addParticipant(participant);

        assertThat(api.listParticipants())
                .contains(participant);
    }
}
