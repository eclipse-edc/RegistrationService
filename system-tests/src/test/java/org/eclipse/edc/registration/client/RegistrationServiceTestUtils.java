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

package org.eclipse.edc.registration.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.ECKey;
import org.eclipse.edc.iam.did.spi.document.DidConstants;
import org.eclipse.edc.iam.did.spi.document.DidDocument;
import org.eclipse.edc.iam.did.spi.document.Service;
import org.eclipse.edc.iam.did.spi.document.VerificationMethod;
import org.eclipse.edc.registration.cli.ClientUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

class RegistrationServiceTestUtils {
    public static final ObjectMapper MAPPER = new ObjectMapper();
    /**
     * The DID that resolves to the sample DID Document for the Dataspace Authority in docker compose (served by the nginx container).
     * Did web format reference: <a href="https://w3c-ccg.github.io/did-method-web/#create-register">...</a>
     */
    static final String DATASPACE_DID_WEB = "did:web:localhost%3A8080:test-dataspace-authority";
    /**
     * Url of IdentityHub of the participant from docker compose (served by the nginx container).
     */
    static final String IDENTITY_HUB_URL = "http://participant:8181/api/identity-hub";

    private RegistrationServiceTestUtils() {
    }

    @NotNull
    private static Service identityHub() {
        return new Service("#identity-hub", "IdentityHub", IDENTITY_HUB_URL);
    }

    static String didDocument() throws Exception {
        var publicKey = (ECKey) ECKey.parseFromPEMEncodedObjects(TestKeyData.PUBLIC_KEY_P256);
        var vm = VerificationMethod.Builder.create()
                .id("#my-key-1")
                .type(DidConstants.ECDSA_SECP_256_K_1_VERIFICATION_KEY_2019)
                .controller("")
                .publicKeyJwk(publicKey.toJSONObject())
                .build();
        var didDocument = DidDocument.Builder.newInstance()
                .verificationMethod(List.of(vm))
                .service(List.of(identityHub()))
                .build();
        return MAPPER.writeValueAsString(didDocument);
    }

    @NotNull
    static String createDid(int apiPort) {
        return "did:web:host.docker.internal%3A" + apiPort;
    }

    @NotNull
    static RegistryApiClient createApi(String did, String apiUrl) {
        return ClientUtils.createApiClient(apiUrl, did, TestKeyData.PRIVATE_KEY_P256);
    }
}
