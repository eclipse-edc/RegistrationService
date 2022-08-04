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

package org.eclipse.dataspaceconnector.registration;

import org.eclipse.dataspaceconnector.api.transformer.DtoTransformerRegistry;
import okhttp3.OkHttpClient;
import org.eclipse.dataspaceconnector.extension.jersey.mapper.EdcApiExceptionMapper;
import org.eclipse.dataspaceconnector.iam.did.spi.key.PrivateKeyWrapper;
import org.eclipse.dataspaceconnector.iam.did.spi.resolution.DidPublicKeyResolver;
import org.eclipse.dataspaceconnector.iam.did.spi.resolution.DidResolverRegistry;
import org.eclipse.dataspaceconnector.identityhub.client.IdentityHubClientImpl;
import org.eclipse.dataspaceconnector.identityhub.credentials.VerifiableCredentialsJwtServiceImpl;
import org.eclipse.dataspaceconnector.registration.api.RegistrationApiController;
import org.eclipse.dataspaceconnector.registration.api.RegistrationService;
import org.eclipse.dataspaceconnector.registration.auth.DidJwtAuthenticationFilter;
import org.eclipse.dataspaceconnector.registration.authority.DummyCredentialsVerifier;
import org.eclipse.dataspaceconnector.registration.authority.spi.CredentialsVerifier;
import org.eclipse.dataspaceconnector.registration.credential.VerifiableCredentialService;
import org.eclipse.dataspaceconnector.registration.credential.VerifiableCredentialServiceImpl;
import org.eclipse.dataspaceconnector.registration.manager.ParticipantManager;
import org.eclipse.dataspaceconnector.registration.store.InMemoryParticipantStore;
import org.eclipse.dataspaceconnector.registration.store.spi.ParticipantStore;
import org.eclipse.dataspaceconnector.registration.transform.ParticipantToParticipantDtoTransformer;
import org.eclipse.dataspaceconnector.spi.EdcException;
import org.eclipse.dataspaceconnector.spi.EdcSetting;
import org.eclipse.dataspaceconnector.spi.WebService;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.security.PrivateKeyResolver;
import org.eclipse.dataspaceconnector.spi.system.ExecutorInstrumentation;
import org.eclipse.dataspaceconnector.spi.system.Inject;
import org.eclipse.dataspaceconnector.spi.system.Provider;
import org.eclipse.dataspaceconnector.spi.system.Requires;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext;

import java.util.Objects;

import static java.lang.String.format;
import static org.eclipse.dataspaceconnector.iam.did.spi.document.DidConstants.DID_URL_SETTING;

/**
 * EDC extension to boot the services used by the Authority Service.
 */
@Requires({PrivateKeyResolver.class, OkHttpClient.class, DidResolverRegistry.class})
public class AuthorityExtension implements ServiceExtension {

    public static final String CONTEXT_ALIAS = "authority";

    @EdcSetting
    private static final String JWT_AUDIENCE_SETTING = "jwt.audience";
    @EdcSetting
    public static final String ERROR_RESPONSE_VERBOSE_SETTING = "edc.error.response.verbose";

    @Inject
    private DidPublicKeyResolver didPublicKeyResolver;

    @Inject
    private Monitor monitor;

    @Inject
    private WebService webService;

    @Inject
    private ParticipantStore participantStore;

    @Inject
    private CredentialsVerifier credentialsVerifier;

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

    private ParticipantManager participantManager;

    @Override
    public void initialize(ServiceExtensionContext context) {
        var audience = Objects.requireNonNull(context.getSetting(JWT_AUDIENCE_SETTING, null),
                () -> format("Missing setting %s", JWT_AUDIENCE_SETTING));
        var errorResponseVerbose = context.getSetting(ERROR_RESPONSE_VERBOSE_SETTING, false);
        var authenticationService = new DidJwtAuthenticationFilter(monitor, didPublicKeyResolver, audience);
        var verifiableCredentialService = verifiableCredentialService(context);

        participantManager = new ParticipantManager(monitor, participantStore, credentialsVerifier, executorInstrumentation, verifiableCredentialService);
        transformerRegistry.register(new ParticipantToParticipantDtoTransformer());

        var registrationService = new RegistrationService(monitor, participantStore, transformerRegistry);
        webService.registerResource(CONTEXT_ALIAS, new RegistrationApiController(registrationService));

        webService.registerResource(CONTEXT_ALIAS, authenticationService);
        webService.registerResource(CONTEXT_ALIAS, new EdcApiExceptionMapper(errorResponseVerbose));
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

    @Provider(isDefault = true)
    public CredentialsVerifier credentialsVerifier() {
        return new DummyCredentialsVerifier();
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
