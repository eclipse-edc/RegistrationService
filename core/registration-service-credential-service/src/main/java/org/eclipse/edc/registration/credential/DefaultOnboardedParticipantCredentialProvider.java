/*
 *  Copyright (c) 2023 Amadeus
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Amadeus - initial API and implementation
 *
 */

package org.eclipse.edc.registration.credential;

import org.eclipse.edc.identityhub.spi.credentials.model.Credential;
import org.eclipse.edc.identityhub.spi.credentials.model.CredentialSubject;
import org.eclipse.edc.registration.spi.credential.OnboardedParticipantCredentialProvider;
import org.eclipse.edc.registration.spi.model.Participant;
import org.eclipse.edc.spi.result.Result;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

/**
 * Provides a default credential for attesting that a participant has onboarded the dataspace.
 */
public class DefaultOnboardedParticipantCredentialProvider implements OnboardedParticipantCredentialProvider {

    private final String dataspaceDid;

    public DefaultOnboardedParticipantCredentialProvider(String dataspaceDid) {
        this.dataspaceDid = dataspaceDid;
    }

    @Override
    public Result<Credential> createCredential(Participant participant) {
        return Result.success(Credential.Builder.newInstance()
                .id(UUID.randomUUID().toString())
                .context("https://www.w3.org/2018/credentials/v1")
                .type("VerifiableCredential")
                .issuer(dataspaceDid)
                .credentialSubject(CredentialSubject.Builder.newInstance()
                        .id(participant.getDid())
                        .claim("memberOfDataspace", dataspaceDid)
                        .build())
                .issuanceDate(Date.from(Instant.now().truncatedTo(ChronoUnit.SECONDS)))
                .build());
    }
}
