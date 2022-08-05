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

package org.eclipse.dataspaceconnector.registration.authority;

import org.eclipse.dataspaceconnector.iam.did.spi.credentials.CredentialsVerifier;
import org.eclipse.dataspaceconnector.iam.did.spi.resolution.DidResolverRegistry;
import org.eclipse.dataspaceconnector.registration.authority.model.Participant;
import org.eclipse.dataspaceconnector.registration.authority.spi.ParticipantVerifier;
import org.eclipse.dataspaceconnector.spi.result.Result;

import static java.lang.String.format;

public class DefaultParticipantVerifier implements ParticipantVerifier {
    private final DidResolverRegistry didResolverRegistry;
    private final CredentialsVerifier verifier;

    public DefaultParticipantVerifier(DidResolverRegistry didResolverRegistry, CredentialsVerifier verifier) {
        this.didResolverRegistry = didResolverRegistry;
        this.verifier = verifier;
    }

    @Override
    public Result<Void> verifyCredentials(Participant participant) {
        var participantDid = participant.getDid();
        var didDocumentResult = didResolverRegistry.resolve(participantDid);
        if (didDocumentResult.failed()) {
            return Result.failure("DID could not be resolved: " + participantDid);
        }
        var result = verifier.getVerifiedCredentials(didDocumentResult.getContent());
        var success = result.succeeded() && !result.getContent().isEmpty();
        return success ? Result.success() : Result.failure(format("Claims from DID [%s] could not be verified", participantDid));
    }
}
