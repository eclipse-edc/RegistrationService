package org.eclipse.dataspaceconnector.registration.api;

import org.eclipse.dataspaceconnector.registration.api.health.HealthApiController;
import org.eclipse.dataspaceconnector.spi.WebService;
import org.eclipse.dataspaceconnector.spi.system.Inject;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext;

/**
 * EDC extension to boot the services used by the Registration Service.
 */
public class RegistrationServiceApiExtension implements ServiceExtension {
    @Inject
    private WebService webService;

    @Override
    public void initialize(ServiceExtensionContext context) {
        var controller = new HealthApiController();
        webService.registerResource(controller);
    }
}
