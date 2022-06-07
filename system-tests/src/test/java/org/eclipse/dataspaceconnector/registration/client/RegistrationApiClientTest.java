package org.eclipse.dataspaceconnector.registration.client;

import org.eclipse.dataspaceconnector.registration.client.api.RegistryApi;
import org.eclipse.dataspaceconnector.registration.client.models.Participant;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@IntegrationTest
public class RegistrationApiClientTest {

    static final String API_URL = "http://localhost:8181/api";

    ApiClient apiClient = ApiClientFactory.createApiClient(API_URL);
    RegistryApi api = new RegistryApi(apiClient);

    @Test
    void listParticipants() {
        await().atMost(Duration.ofSeconds(30))
                .untilAsserted(() ->
                        assertThat(api.listParticipants())
                        .extracting(Participant::getName)
                        .containsExactlyInAnyOrder("consumer-eu", "consumer-us", "provider"));

    }
}
