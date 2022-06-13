/*
 *  Copyright (c) 2022 Microsoft Corporation
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Microsoft Corporation - initial API and implementation
 *
 */

package org.eclipse.dataspaceconnector.registration.api;

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
        var monitor = context.getMonitor();
        var participantStore = new InMemoryParticipantStore();
        var registrationService = new RegistrationService(monitor, participantStore);
        webService.registerResource(new RegistrationApiController(registrationService));
    }
}
