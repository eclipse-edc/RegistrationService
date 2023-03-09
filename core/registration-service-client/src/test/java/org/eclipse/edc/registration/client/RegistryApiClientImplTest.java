package org.eclipse.edc.registration.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.failsafe.RetryPolicy;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.eclipse.edc.connector.core.base.EdcHttpClientImpl;
import org.eclipse.edc.registration.client.model.ParticipantDto;
import org.eclipse.edc.spi.monitor.Monitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class RegistryApiClientImplTest {

    RegistryApiClientImpl apiClient;
    private MockWebServer mockServer;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() throws IOException {
        objectMapper = new ObjectMapper();
        mockServer = new MockWebServer();

        var m = mock(Monitor.class);
        var url = mockServer.url("/api/v1");
        apiClient = new RegistryApiClientImpl(new EdcHttpClientImpl(new OkHttpClient(), RetryPolicy.ofDefaults(), m), url.toString(), new ObjectMapper());
    }

    @Test
    void addParticipant() throws InterruptedException {

        mockServer.enqueue(new MockResponse());
        var result = apiClient.addParticipant();
        assertThat(result.succeeded()).isTrue();

        var rs = mockServer.takeRequest();
        assertThat(rs.getPath()).isEqualTo("/api/v1/registry/participant");
        assertThat(rs.getMethod()).isEqualTo("POST");
        assertThat(result.succeeded()).isTrue();
    }

    @Test
    void listParticipants() throws InterruptedException, IOException {
        var dto = new ParticipantDto("test-did", ParticipantDto.OnboardingStatus.ONBOARDED);
        var body = objectMapper.writeValueAsString(List.of(dto));
        mockServer.enqueue(new MockResponse().setBody(body));
        var result = apiClient.listParticipants();

        assertThat(result.getContent()).hasSize(1).usingRecursiveFieldByFieldElementComparator().containsOnly(dto);

        var rs = mockServer.takeRequest();
        assertThat(rs.getPath()).isEqualTo("/api/v1/registry/participants");
        assertThat(rs.getMethod()).isEqualTo("GET");
    }

    @Test
    void getParticipant() throws InterruptedException, JsonProcessingException {
        var dto = new ParticipantDto("test-did", ParticipantDto.OnboardingStatus.ONBOARDED);
        mockServer.enqueue(new MockResponse().setBody(objectMapper.writeValueAsString(dto)));

        var result = apiClient.getParticipant();
        assertThat(result.getContent()).usingRecursiveComparison().isEqualTo(dto);

        var rs = mockServer.takeRequest();
        assertThat(rs.getPath()).isEqualTo("/api/v1/registry/participant");
        assertThat(rs.getMethod()).isEqualTo("GET");
    }
}