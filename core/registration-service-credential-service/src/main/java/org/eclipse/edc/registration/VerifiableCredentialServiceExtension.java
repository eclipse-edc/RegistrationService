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

import org.eclipse.edc.iam.did.spi.key.PrivateKeyWrapper;
import org.eclipse.edc.iam.did.spi.resolution.DidResolverRegistry;
import org.eclipse.edc.identityhub.client.IdentityHubClientImpl;
import org.eclipse.edc.identityhub.credentials.jwt.JwtCredentialEnvelopeTransformer;
import org.eclipse.edc.identityhub.credentials.jwt.JwtCredentialFactory;
import org.eclipse.edc.identityhub.spi.credentials.transformer.CredentialEnvelopeTransformerRegistryImpl;
import org.eclipse.edc.registration.credential.DefaultOnboardedParticipantCredentialProvider;
import org.eclipse.edc.registration.service.VerifiableCredentialServiceImpl;
import org.eclipse.edc.registration.spi.credential.OnboardedParticipantCredentialProvider;
import org.eclipse.edc.registration.spi.service.VerifiableCredentialService;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Provider;
import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.spi.http.EdcHttpClient;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.security.PrivateKeyResolver;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.types.TypeManager;

import java.util.Objects;

import static java.lang.String.format;
import static org.eclipse.edc.iam.did.spi.document.DidConstants.DID_URL_SETTING;

@Extension(VerifiableCredentialServiceExtension.NAME)
public class VerifiableCredentialServiceExtension implements ServiceExtension {

    public static final String NAME = "Verifiable Credential Service";

    @Inject
    private Monitor monitor;

    @Inject
    private EdcHttpClient httpClient;

    @Inject
    private DidResolverRegistry didResolverRegistry;

    @Inject
    private PrivateKeyResolver privateKeyResolver;

    @Inject
    private OnboardedParticipantCredentialProvider credentialProvider;

    @Inject
    private TypeManager typeManager;

    @Override
    public String name() {
        return NAME;
    }

    @Provider
    public VerifiableCredentialService verifiableCredentialService(ServiceExtensionContext context) {
        var mapper = typeManager.getMapper();

        var credentialEnvelopeTransformerRegistry = new CredentialEnvelopeTransformerRegistryImpl();
        credentialEnvelopeTransformerRegistry.register(new JwtCredentialEnvelopeTransformer(mapper));
        var identityHubClient = new IdentityHubClientImpl(httpClient, typeManager, credentialEnvelopeTransformerRegistry);

        var privateKeyWrapper = privateKeyResolver.resolvePrivateKey(context.getConnectorId(), PrivateKeyWrapper.class);
        Objects.requireNonNull(privateKeyWrapper, "Couldn't resolve private key from connector " + context.getConnectorId());

        var jwtCredentialFactory = new JwtCredentialFactory(mapper);

        return new VerifiableCredentialServiceImpl(monitor, privateKeyWrapper, didResolverRegistry, identityHubClient, credentialProvider, jwtCredentialFactory);
    }

    @Provider(isDefault = true)
    public OnboardedParticipantCredentialProvider onboardedParticipantCredentialProvider(ServiceExtensionContext context) {
        var dataspaceDid = context.getSetting(DID_URL_SETTING, null);
        if (dataspaceDid == null) {
            throw new EdcException(format("The DID Url setting '(%s)' was null!", DID_URL_SETTING));
        }
        return new DefaultOnboardedParticipantCredentialProvider(dataspaceDid);
    }
}
