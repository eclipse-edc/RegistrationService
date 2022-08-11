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

package org.eclipse.dataspaceconnector.registration.verifier;

import org.eclipse.dataspaceconnector.iam.did.spi.credentials.CredentialsVerifier;
import org.eclipse.dataspaceconnector.iam.did.spi.resolution.DidResolverRegistry;
import org.eclipse.dataspaceconnector.registration.authority.spi.ParticipantVerifier;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.response.StatusResult;

import static java.lang.String.format;
import static org.eclipse.dataspaceconnector.spi.response.ResponseStatus.ERROR_RETRY;

/**
 * Implementation of {@link ParticipantVerifier} that only retrieves verifiable credentials,
 * but performs no action on them. It authorizes any participant to onboard to the dataspace,
 * as long as its Identity Hub can be accessed.
 * <p>
 * This is meant as a starting point for implementing custom dataspace onboarding logic.
 */
public class DefaultParticipantVerifier implements ParticipantVerifier {

    private final Monitor monitor;
    private final DidResolverRegistry resolverRegistry;
    private final CredentialsVerifier credentialsVerifier;

    public DefaultParticipantVerifier(Monitor monitor, DidResolverRegistry resolverRegistry, CredentialsVerifier credentialsVerifier) {
        this.monitor = monitor;
        this.resolverRegistry = resolverRegistry;
        this.credentialsVerifier = credentialsVerifier;
    }

    @Override
    public StatusResult<Boolean> verifyCredentials(String did) {
        monitor.info(() -> "Get credentials VC for " + did);

        var didDocument = resolverRegistry.resolve(did);
        if (didDocument.failed()) {
            return StatusResult.failure(ERROR_RETRY, "Failed to resolve DID " + did + ". " + didDocument.getFailureDetail());
        }

        var vcResult = credentialsVerifier.getVerifiedCredentials(didDocument.getContent());
        if (vcResult.failed()) {
            return StatusResult.failure(ERROR_RETRY, "Failed to retrieve VCs. " + vcResult.getFailureDetail());
        }
        var credentials = vcResult.getContent();

        monitor.info(() -> format("Retrieved VCs for %s: %s", did, credentials));
        return StatusResult.success(true);
    }
}
