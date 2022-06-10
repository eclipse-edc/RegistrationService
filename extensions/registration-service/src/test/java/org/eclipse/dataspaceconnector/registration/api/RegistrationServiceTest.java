package org.eclipse.dataspaceconnector.registration.api;

import org.eclipse.dataspaceconnector.registration.store.model.Participant;
import org.eclipse.dataspaceconnector.registration.store.model.ParticipantStatus;
import org.eclipse.dataspaceconnector.registration.store.spi.ParticipantStore;
import org.eclipse.dataspaceconnector.spi.monitor.ConsoleMonitor;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.system.ExecutorInstrumentation;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.dataspaceconnector.registration.TestUtils.createParticipant;
import static org.eclipse.dataspaceconnector.registration.store.model.ParticipantStatus.AUTHORIZED;
import static org.eclipse.dataspaceconnector.registration.store.model.ParticipantStatus.ONBOARDING_INITIATED;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RegistrationServiceTest {

    Monitor monitor = new ConsoleMonitor();
    ParticipantStore participantStore = mock(ParticipantStore.class);
    RegistrationService service = new RegistrationService(monitor, participantStore, ExecutorInstrumentation.noop());
    Participant.Builder participantBuilder = createParticipant();
    ArgumentCaptor<Participant> captor = ArgumentCaptor.forClass(Participant.class);

    @Test
    void listParticipants_empty() {
        assertThat(service.listParticipants()).isEmpty();
    }

    @Test
    void listParticipants() {
        var participant = participantBuilder.build();
        when(participantStore.listParticipants()).thenReturn(List.of(participant));
        assertThat(service.listParticipants()).containsExactly(participant);
    }

    @Test
    void addParticipant() {
        var participant = participantBuilder.build();
        service.addParticipant(participant);
        verify(participantStore).save(participant);
    }

    @Test
    void advancesStateFromOnboardingInitiatedToAuthorized() throws InterruptedException {
        var participant = participantBuilder.status(ONBOARDING_INITIATED).build();
        when(participantStore.nextForState(eq(ONBOARDING_INITIATED), anyInt()))
                .thenReturn(List.of(participant), List.of());

        var latch = new CountDownLatch(1);
        service.registerListener(new ParticipantListener() {
            @Override
            public void preAuthorized(Participant participant) {
                latch.countDown();
            }
        });

        service.start();
        assertThat(latch.await(10, SECONDS)).isTrue();

        verify(participantStore).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(AUTHORIZED);
        assertThat(captor.getValue())
                .usingRecursiveComparison()
                .ignoringFieldsOfTypes(ParticipantStatus.class)
                .isEqualTo(participant);
    }
}