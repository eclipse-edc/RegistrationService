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

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.edc.registration.ParticipantUtils.createParticipant;

class DefaultOnboardedParticipantCredentialProviderTest {

    @Test
    void verifyCreateCredential() {
        var dataspaceDid = "test";
        var provider = new DefaultOnboardedParticipantCredentialProvider(dataspaceDid);
        var participant = createParticipant().build();

        var result = provider.createCredential(participant);
        assertThat(result.succeeded()).isTrue();
        var credential = result.getContent();
        assertThat(credential.getTypes()).containsExactly("VerifiableCredential");
        assertThat(credential.getContexts()).containsExactly("https://www.w3.org/2018/credentials/v1");
        assertThat(credential.getIssuer()).isEqualTo(dataspaceDid);
        assertThat(credential.getCredentialSubject().getId()).isEqualTo(participant.getDid());
        assertThat(credential.getCredentialSubject().getClaims()).containsExactlyEntriesOf(Map.of("memberOfDataspace", dataspaceDid));
    }
}