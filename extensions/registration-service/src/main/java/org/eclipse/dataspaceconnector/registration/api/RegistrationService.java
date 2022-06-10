package org.eclipse.dataspaceconnector.registration.api;

import org.eclipse.dataspaceconnector.common.statemachine.StateMachine;
import org.eclipse.dataspaceconnector.common.statemachine.StateProcessorImpl;
import org.eclipse.dataspaceconnector.registration.store.model.Participant;
import org.eclipse.dataspaceconnector.registration.store.model.ParticipantStatus;
import org.eclipse.dataspaceconnector.registration.store.spi.ParticipantStore;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.observe.Observable;
import org.eclipse.dataspaceconnector.spi.observe.ObservableImpl;
import org.eclipse.dataspaceconnector.spi.retry.WaitStrategy;
import org.eclipse.dataspaceconnector.spi.system.ExecutorInstrumentation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.eclipse.dataspaceconnector.registration.store.model.ParticipantStatus.ONBOARDING_INITIATED;

/**
 * Registration service for dataspace participants.
 */
public class RegistrationService {

    private final Monitor monitor;
    private final ParticipantStore participantStore;
    private final StateMachine stateMachine;
    private final Observable<ParticipantListener> observable = new ObservableImpl<>();
    private final int batchSize = 5;

    public RegistrationService(Monitor monitor, ParticipantStore participantStore, ExecutorInstrumentation executorInstrumentation) {
        this.monitor = monitor;
        this.participantStore = participantStore;

        // default wait five seconds
        WaitStrategy waitStrategy = () -> 5000L;
        stateMachine = StateMachine.Builder.newInstance("registration-service", monitor, executorInstrumentation, waitStrategy)
                .processor(processNegotiationsInState(ONBOARDING_INITIATED, this::processOnboardingInitiated))
                .build();
    }

    public void registerListener(ParticipantListener listener){
        observable.registerListener(listener);
    }

    private Boolean processOnboardingInitiated(Participant participant) {
        participant.transitionAuthorized();
        update(participant, o -> o.preAuthorized(participant));
        return true;
    }

    private void update(Participant participant, Consumer<ParticipantListener> observe) {
        observable.invokeForEach(observe);
        participantStore.save(participant);
    }

    private StateProcessorImpl<Participant> processNegotiationsInState(ParticipantStatus status, Function<Participant, Boolean> function) {
        return new StateProcessorImpl<>(() -> participantStore.nextForState(status, batchSize), function);
    }

    /**
     * Lists all dataspace participants.
     *
     * @return list of dataspace participants.
     */
    public List<Participant> listParticipants() {
        monitor.info("List all participants of the dataspace.");
        return new ArrayList<>(participantStore.listParticipants());
    }

    public void addParticipant(Participant participant) {
        monitor.info("Adding a participant in the dataspace.");
        update(participant, o -> o.onCreation(participant));
    }

    public void start() {
        stateMachine.start();
    }

    public void stop() {
        stateMachine.stop();
    }
}
