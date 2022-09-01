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

package org.eclipse.dataspaceconnector.registration.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.ECKey;
import okhttp3.OkHttpClient;
import org.eclipse.dataspaceconnector.iam.did.spi.document.DidConstants;
import org.eclipse.dataspaceconnector.iam.did.spi.document.DidDocument;
import org.eclipse.dataspaceconnector.iam.did.spi.document.EllipticCurvePublicKey;
import org.eclipse.dataspaceconnector.iam.did.spi.document.Service;
import org.eclipse.dataspaceconnector.iam.did.spi.document.VerificationMethod;
import org.eclipse.dataspaceconnector.identityhub.client.IdentityHubClientImpl;
import org.eclipse.dataspaceconnector.identityhub.credentials.VerifiableCredentialsJwtServiceImpl;
import org.eclipse.dataspaceconnector.identityhub.credentials.model.VerifiableCredential;
import org.eclipse.dataspaceconnector.junit.testfixtures.TestUtils;
import org.eclipse.dataspaceconnector.registration.cli.ClientUtils;
import org.eclipse.dataspaceconnector.registration.cli.CryptoUtils;
import org.eclipse.dataspaceconnector.registration.client.api.RegistryApi;
import org.eclipse.dataspaceconnector.spi.monitor.ConsoleMonitor;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RegistrationServiceTestUtils {
    static final ObjectMapper MAPPER = new ObjectMapper();
    /**
     * The <a href="https://w3c-ccg.github.io/did-method-web/#create-register">Web DID</a>
     * that resolves to the sample DID Document for the Dataspace Authority from the test runtime.
     */
    static final String DATASPACE_DID_WEB_LOCAL = "did:web:localhost%3A8080:test-dataspace-authority";

    /**
     * The Web DID that resolves to the sample DID Document for the Dataspace Authority in docker compose (served by the nginx container).
     */
    static final String DATASPACE_DID_WEB_DOCKER = createDid(8080) + ":test-dataspace-authority";

    /**
     * URL of IdentityHub of the participant from the test runtime.
     */
    static final String HUB_BASE_URL_LOCAL = "http://localhost:8181/api/identity-hub";

    /**
     * URL of IdentityHub of the participant from docker compose (served by the nginx container).
     */
    static final String HUB_BASE_URL_DOCKER = "http://participant:8181/api/identity-hub";

    static final Monitor MONITOR = new ConsoleMonitor();

    static OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
    static IdentityHubClientImpl identityHubClient = new IdentityHubClientImpl(okHttpClient, new ObjectMapper(), MONITOR);
    static VerifiableCredentialsJwtServiceImpl jwtService = new VerifiableCredentialsJwtServiceImpl(MAPPER, MONITOR);
    private static String enrollmentCredentialId = "5067AB66-61A4-4F8E-ADE0-7BDF376F8DB5";

    private RegistrationServiceTestUtils() {
    }

    static String didDocument(String did) throws Exception {
        var publicKey = (ECKey) ECKey.parseFromPEMEncodedObjects(TestKeyData.PUBLIC_KEY_P256);
        var vm = VerificationMethod.Builder.create()
                .id("#my-key-1")
                .type(DidConstants.ECDSA_SECP_256_K_1_VERIFICATION_KEY_2019)
                .controller("")
                .publicKeyJwk(new EllipticCurvePublicKey(publicKey.getCurve().getName(), publicKey.getKeyType().getValue(), publicKey.getX().toString(), publicKey.getY().toString()))
                .build();
        var didDocument = DidDocument.Builder.newInstance()
                .id(did)
                .verificationMethod(List.of(vm))
                .service(List.of(identityHub()))
                .build();
        return MAPPER.writeValueAsString(didDocument);
    }

    @NotNull
    static String createDid(int apiPort) {
        // host.docker.internal is used in docker-compose file to connect from Registration Service container to a mock-service on the host
        return "did:web:host.docker.internal%3A" + apiPort;
    }

    @NotNull
    static RegistryApi createApi(String did, String apiUrl) {
        var apiClient = ClientUtils.createApiClient(apiUrl, did, TestKeyData.PRIVATE_KEY_P256);
        return new RegistryApi(apiClient);
    }

    @NotNull
    private static Service identityHub() {
        return new Service("#identity-hub", "IdentityHub", HUB_BASE_URL_DOCKER);
    }

    static void addEnrollmentCredential(String did) throws Exception {
        var key = Files.readString(new File(TestUtils.findBuildRoot(), "resources/vault/private-key.pem").toPath());
        var authorityPrivateKey = CryptoUtils.parseFromPemEncodedObjects(key);
        var vc = VerifiableCredential.Builder.newInstance()
                .id(enrollmentCredentialId)
                .credentialSubject(Map.of("gaiaXMember", "true"))
                .build();

        var jwt = jwtService.buildSignedJwt(vc, DATASPACE_DID_WEB_DOCKER, did, authorityPrivateKey);
        var addVcResult = identityHubClient.addVerifiableCredential(HUB_BASE_URL_LOCAL, jwt);
        assertThat(addVcResult.succeeded()).isTrue();
    }
}
