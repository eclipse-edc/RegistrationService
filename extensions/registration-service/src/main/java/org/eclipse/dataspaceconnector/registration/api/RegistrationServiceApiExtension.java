package org.eclipse.dataspaceconnector.registration.api;

import org.eclipse.dataspaceconnector.spi.WebService;
import org.eclipse.dataspaceconnector.spi.system.Inject;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext;

import java.nio.file.Path;
import java.util.Objects;

import static org.eclipse.dataspaceconnector.common.configuration.ConfigurationFunctions.propOrEnv;

/**
 * EDC extension to boot the services used by the Registration Service.
 */
public class RegistrationServiceApiExtension implements ServiceExtension {

    @Inject
    private WebService webService;

    @Override
    public void initialize(ServiceExtensionContext context) {
        var nodeJsonPath = Path.of(Objects.requireNonNull(propOrEnv("NODES_JSON_DIR", "registry"), "Env var NODES_JSON_DIR is null"));
        var nodeJsonPrefix = Objects.requireNonNull(propOrEnv("NODES_JSON_FILES_PREFIX", "registry-"), "Env var NODES_JSON_FILES_PREFIX is null");

        var typeManager = context.getTypeManager();
        var monitor = context.getMonitor();
        var registrationService = new RegistrationService(nodeJsonPath, nodeJsonPrefix, typeManager, monitor);
        webService.registerResource(new RegistrationApiController(registrationService, monitor));
    }
}
