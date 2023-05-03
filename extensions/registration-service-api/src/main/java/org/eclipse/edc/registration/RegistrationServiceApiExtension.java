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

package org.eclipse.edc.registration;

import org.eclipse.edc.iam.did.spi.resolution.DidPublicKeyResolver;
import org.eclipse.edc.registration.api.RegistrationServiceApiController;
import org.eclipse.edc.registration.auth.DidJwtAuthenticationFilter;
import org.eclipse.edc.registration.spi.registration.RegistrationService;
import org.eclipse.edc.registration.transform.ParticipantToParticipantDtoTransformer;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.transform.spi.TypeTransformerRegistry;
import org.eclipse.edc.web.jersey.mapper.EdcApiExceptionMapper;
import org.eclipse.edc.web.spi.WebService;

import java.util.Objects;

import static java.lang.String.format;

@Extension(RegistrationServiceApiExtension.NAME)
public class RegistrationServiceApiExtension implements ServiceExtension {

    public static final String NAME = "Registration Service API";

    private static final String CONTEXT_ALIAS = "authority";

    @Setting
    private static final String JWT_AUDIENCE_SETTING = "jwt.audience";

    @Inject
    private DidPublicKeyResolver didPublicKeyResolver;

    @Inject
    private Monitor monitor;

    @Inject
    private WebService webService;

    @Inject
    private TypeTransformerRegistry transformerRegistry;

    @Inject
    private RegistrationService registrationService;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        var audience = Objects.requireNonNull(context.getSetting(JWT_AUDIENCE_SETTING, null),
                () -> format("Missing setting %s", JWT_AUDIENCE_SETTING));
        var authenticationService = new DidJwtAuthenticationFilter(monitor, didPublicKeyResolver, audience);

        transformerRegistry.register(new ParticipantToParticipantDtoTransformer());

        webService.registerResource(CONTEXT_ALIAS, new RegistrationServiceApiController(registrationService, transformerRegistry));
        webService.registerResource(CONTEXT_ALIAS, authenticationService);
        webService.registerResource(CONTEXT_ALIAS, new EdcApiExceptionMapper());
    }
}
