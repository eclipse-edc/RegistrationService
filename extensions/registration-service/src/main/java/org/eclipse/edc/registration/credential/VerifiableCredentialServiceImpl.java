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

package org.eclipse.edc.registration.credential;

import com.nimbusds.jwt.SignedJWT;
import org.eclipse.edc.iam.did.spi.document.DidDocument;
import org.eclipse.edc.iam.did.spi.key.PrivateKeyWrapper;
import org.eclipse.edc.iam.did.spi.resolution.DidResolverRegistry;
import org.eclipse.edc.identityhub.client.spi.IdentityHubClient;
import org.eclipse.edc.identityhub.credentials.jwt.JwtCredentialEnvelope;
import org.eclipse.edc.identityhub.spi.credentials.model.VerifiableCredential;
import org.eclipse.edc.identityhub.verifier.jwt.VerifiableCredentialsJwtService;
import org.eclipse.edc.registration.authority.model.Participant;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.response.ResponseStatus;
import org.eclipse.edc.spi.response.StatusResult;
import org.eclipse.edc.spi.result.Result;

import java.util.Map;
import java.util.UUID;

import static org.eclipse.edc.spi.response.ResponseStatus.ERROR_RETRY;
import static org.eclipse.edc.spi.response.ResponseStatus.FATAL_ERROR;

public class VerifiableCredentialServiceImpl implements VerifiableCredentialService {
    private static final String IDENTITY_HUB_SERVICE_TYPE = "IdentityHub";

    private final Monitor monitor;
    private final VerifiableCredentialsJwtService jwtService;
    private final PrivateKeyWrapper privateKeyWrapper;
    private final String dataspaceDid;
    private final DidResolverRegistry resolverRegistry;
    private final IdentityHubClient identityHubClient;

    public VerifiableCredentialServiceImpl(Monitor monitor, VerifiableCredentialsJwtService jwtService, PrivateKeyWrapper privateKeyWrapper, String dataspaceDid, DidResolverRegistry resolverRegistry, IdentityHubClient identityHubClient) {
        this.monitor = monitor;
        this.jwtService = jwtService;
        this.privateKeyWrapper = privateKeyWrapper;
        this.dataspaceDid = dataspaceDid;
        this.resolverRegistry = resolverRegistry;
        this.identityHubClient = identityHubClient;
    }

    @Override
    public StatusResult<Void> pushVerifiableCredential(Participant participant) {
        var vc = VerifiableCredential.Builder.newInstance()
                .id(UUID.randomUUID().toString())
                .credentialSubject(Map.of("memberOfDataspace", dataspaceDid))
                .build();

        var did = participant.getDid();

        monitor.debug("Building VC JWT");
        SignedJWT jwt;
        try {
            jwt = jwtService.buildSignedJwt(vc, dataspaceDid, did, privateKeyWrapper);
        } catch (Exception e) {
            return failureResult(FATAL_ERROR, e.toString());
        }

        monitor.debug(() -> "Resolving DID Document for " + did);
        var didDocument = resolverRegistry.resolve(did);
        if (didDocument.failed()) {
            return failureResult(ERROR_RETRY, "Failed to resolve DID " + did + ". " + didDocument.getFailureDetail());
        }
        var identityHubUrlResult = getIdentityHubBaseUrl(didDocument.getContent());
        if (identityHubUrlResult.failed()) {
            return failureResult(FATAL_ERROR, "Failed to resolve Identity Hub URL from DID document for " + did);
        }
        String identityHubUrl = identityHubUrlResult.getContent();

        monitor.debug(() -> "Sending VC to identity hub " + identityHubUrl);
        var addVcResult = identityHubClient.addVerifiableCredential(identityHubUrl, new JwtCredentialEnvelope(jwt));
        if (addVcResult.failed()) {
            return failureResult(ERROR_RETRY, "Failed to send VC. " + addVcResult.getFailureDetail());
        }

        monitor.info("Sent dataspace membership VC for " + did);
        return StatusResult.success();
    }

    private Result<String> getIdentityHubBaseUrl(DidDocument didDocument) {
        var hubBaseUrl = didDocument
                .getService()
                .stream()
                .filter(s -> s.getType().equals(IDENTITY_HUB_SERVICE_TYPE))
                .findFirst();

        return hubBaseUrl.map(u -> Result.success(u.getServiceEndpoint()))
                .orElse(Result.failure("Failed getting Identity Hub URL"));
    }

    private StatusResult<Void> failureResult(ResponseStatus status, String message) {
        monitor.warning(message);
        return StatusResult.failure(status, message);
    }
}
