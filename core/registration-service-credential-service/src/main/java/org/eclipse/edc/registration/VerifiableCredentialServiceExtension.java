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
import org.eclipse.edc.iam.did.spi.key.PrivateKeyWrapper;
import org.eclipse.edc.iam.did.spi.resolution.DidResolverRegistry;
import org.eclipse.edc.identityhub.client.IdentityHubClientImpl;
import org.eclipse.edc.identityhub.credentials.jwt.JwtCredentialEnvelopeTransformer;
import org.eclipse.edc.identityhub.spi.credentials.transformer.CredentialEnvelopeTransformerRegistryImpl;
import org.eclipse.edc.registration.credential.DefaultOnboardedParticipantCredentialProvider;
import org.eclipse.edc.registration.service.VerifiableCredentialServiceImpl;
import org.eclipse.edc.registration.spi.credential.OnboardedParticipantCredentialProvider;
import org.eclipse.edc.registration.spi.service.VerifiableCredentialService;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Provider;
import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.security.PrivateKeyResolver;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;

import java.util.Objects;

import static java.lang.String.format;
import static org.eclipse.edc.iam.did.spi.document.DidConstants.DID_URL_SETTING;

@Extension(VerifiableCredentialServiceExtension.NAME)
public class VerifiableCredentialServiceExtension implements ServiceExtension {

    public static final String NAME = "Verifiable Credential Service";

    @Inject
    private Monitor monitor;

    @Inject
    private OkHttpClient httpClient;

    @Inject
    private DidResolverRegistry didResolverRegistry;

    @Inject
    private PrivateKeyResolver privateKeyResolver;

    @Inject
    private OnboardedParticipantCredentialProvider credentialProvider;

    @Override
    public String name() {
        return NAME;
    }

    @Provider
    public VerifiableCredentialService verifiableCredentialService(ServiceExtensionContext context) {
        var dataspaceDid = getDataspaceDid(context);

        var mapper = context.getTypeManager().getMapper();

        var credentialEnvelopeTransformerRegistry = new CredentialEnvelopeTransformerRegistryImpl();
        credentialEnvelopeTransformerRegistry.register(new JwtCredentialEnvelopeTransformer(mapper));
        var identityHubClient = new IdentityHubClientImpl(httpClient, mapper, monitor, credentialEnvelopeTransformerRegistry);

        var privateKeyWrapper = privateKeyResolver.resolvePrivateKey(context.getConnectorId(), PrivateKeyWrapper.class);
        Objects.requireNonNull(privateKeyWrapper, "Couldn't resolve private key from connector " + context.getConnectorId());

        return new VerifiableCredentialServiceImpl(monitor, privateKeyWrapper, dataspaceDid, didResolverRegistry, identityHubClient, credentialProvider, mapper);
    }

    @Provider(isDefault = true)
    public OnboardedParticipantCredentialProvider onboardedParticipantCredentialProvider(ServiceExtensionContext context) {
        var dataspaceDid = getDataspaceDid(context);
        return new DefaultOnboardedParticipantCredentialProvider(dataspaceDid);
    }

    private static String getDataspaceDid(ServiceExtensionContext context) {
        var did = context.getSetting(DID_URL_SETTING, null);
        if (did == null) {
            throw new EdcException(format("The DID Url setting '(%s)' was null!", DID_URL_SETTING));
        }
        return did;
    }
}
