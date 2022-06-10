package org.eclipse.dataspaceconnector.registration.api;

import org.eclipse.dataspaceconnector.registration.store.spi.ParticipantStore;
import org.eclipse.dataspaceconnector.spi.WebService;
import org.eclipse.dataspaceconnector.spi.system.ExecutorInstrumentation;
import org.eclipse.dataspaceconnector.spi.system.Inject;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext;

/**
 * EDC extension to boot the services used by the Registration Service.
 */
public class RegistrationServiceApiExtension implements ServiceExtension {

    @Inject
    private WebService webService;

    @Inject
    private ParticipantStore participantStore;

    @Inject
    private ExecutorInstrumentation executorInstrumentation;

    private RegistrationService registrationService;

    @Override
    public void initialize(ServiceExtensionContext context) {
        var monitor = context.getMonitor();
        registrationService = new RegistrationService(monitor, participantStore, executorInstrumentation);
        webService.registerResource(new RegistrationApiController(registrationService));
    }

    @Override
    public void start() {
        registrationService.start();
    }

    @Override
    public void shutdown() {
        registrationService.stop();
    }
}
