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

import okhttp3.OkHttpClient;
import org.eclipse.edc.api.transformer.DtoTransformerRegistry;
import org.eclipse.edc.iam.did.spi.credentials.CredentialsVerifier;
import org.eclipse.edc.iam.did.spi.key.PrivateKeyWrapper;
import org.eclipse.edc.iam.did.spi.resolution.DidPublicKeyResolver;
import org.eclipse.edc.iam.did.spi.resolution.DidResolverRegistry;
import org.eclipse.edc.identityhub.client.IdentityHubClientImpl;
import org.eclipse.edc.identityhub.spi.credentials.VerifiableCredentialsJwtServiceImpl;
import org.eclipse.edc.registration.api.RegistrationApiController;
import org.eclipse.edc.registration.api.RegistrationService;
import org.eclipse.edc.registration.auth.DidJwtAuthenticationFilter;
import org.eclipse.edc.registration.authority.spi.ParticipantVerifier;
import org.eclipse.edc.registration.credential.VerifiableCredentialService;
import org.eclipse.edc.registration.credential.VerifiableCredentialServiceImpl;
import org.eclipse.edc.registration.manager.ParticipantManager;
import org.eclipse.edc.registration.store.InMemoryParticipantStore;
import org.eclipse.edc.registration.store.spi.ParticipantStore;
import org.eclipse.edc.registration.transform.ParticipantToParticipantDtoTransformer;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Provider;
import org.eclipse.edc.runtime.metamodel.annotation.Requires;
import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.security.PrivateKeyResolver;
import org.eclipse.edc.spi.system.ExecutorInstrumentation;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.telemetry.Telemetry;
import org.eclipse.edc.web.jersey.mapper.EdcApiExceptionMapper;
import org.eclipse.edc.web.spi.WebService;

import java.util.Objects;

import static java.lang.String.format;
import static org.eclipse.edc.iam.did.spi.document.DidConstants.DID_URL_SETTING;

/**
 * EDC extension to boot the services used by the Authority Service.
 */
@Requires({ PrivateKeyResolver.class, OkHttpClient.class, DidResolverRegistry.class, CredentialsVerifier.class })
public class AuthorityExtension implements ServiceExtension {

    public static final String CONTEXT_ALIAS = "authority";

    @Setting
    private static final String JWT_AUDIENCE_SETTING = "jwt.audience";

    @Inject
    private DidPublicKeyResolver didPublicKeyResolver;

    @Inject
    private Monitor monitor;

    @Inject
    private WebService webService;

    @Inject
    private ParticipantStore participantStore;

    @Inject
    private ParticipantVerifier participantVerifier;

    @Inject
    private ExecutorInstrumentation executorInstrumentation;

    @Inject
    private DidResolverRegistry resolverRegistry;

    @Inject
    private OkHttpClient httpClient;

    @Inject
    private PrivateKeyResolver privateKeyResolver;

    @Inject
    private DtoTransformerRegistry transformerRegistry;

    @Inject
    private Telemetry telemetry;

    private ParticipantManager participantManager;

    @Override
    public void initialize(ServiceExtensionContext context) {
        var audience = Objects.requireNonNull(context.getSetting(JWT_AUDIENCE_SETTING, null),
                () -> format("Missing setting %s", JWT_AUDIENCE_SETTING));
        var authenticationService = new DidJwtAuthenticationFilter(monitor, didPublicKeyResolver, audience);
        var verifiableCredentialService = verifiableCredentialService(context);

        participantManager = new ParticipantManager(monitor, participantStore, participantVerifier, executorInstrumentation, verifiableCredentialService, telemetry);
        transformerRegistry.register(new ParticipantToParticipantDtoTransformer());

        var registrationService = new RegistrationService(monitor, participantStore, transformerRegistry, telemetry);
        webService.registerResource(CONTEXT_ALIAS, new RegistrationApiController(registrationService));

        webService.registerResource(CONTEXT_ALIAS, authenticationService);
        webService.registerResource(CONTEXT_ALIAS, new EdcApiExceptionMapper());
    }

    @Override
    public void start() {
        participantManager.start();
    }

    @Override
    public void shutdown() {
        participantManager.stop();
    }

    @Provider(isDefault = true)
    public ParticipantStore participantStore() {
        return new InMemoryParticipantStore();
    }

    private VerifiableCredentialService verifiableCredentialService(ServiceExtensionContext context) {
        var didUrl = context.getSetting(DID_URL_SETTING, null);
        if (didUrl == null) {
            throw new EdcException(format("The DID Url setting '(%s)' was null!", DID_URL_SETTING));
        }

        var mapper = context.getTypeManager().getMapper();
        var jwtService = new VerifiableCredentialsJwtServiceImpl(mapper, monitor);

        var identityHubClient = new IdentityHubClientImpl(httpClient, mapper, monitor);

        var privateKeyWrapper = privateKeyResolver.resolvePrivateKey(context.getConnectorId(), PrivateKeyWrapper.class);
        Objects.requireNonNull(privateKeyWrapper, "Couldn't resolve private key from connector " + context.getConnectorId());

        return new VerifiableCredentialServiceImpl(monitor, jwtService, privateKeyWrapper, didUrl, resolverRegistry, identityHubClient);
    }
}
