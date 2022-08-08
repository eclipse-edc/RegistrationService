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

import com.github.javafaker.Faker;
import org.eclipse.dataspaceconnector.registration.cli.ClientUtils;
import org.eclipse.dataspaceconnector.registration.client.api.RegistryApi;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
public class RegistrationApiClientTest {
    static final String CLIENT_DID_WEB = "did:web:did-server:test-client";
    static final String API_URL = "http://localhost:8182/authority";
    static final Faker FAKER = new Faker();
    static RegistryApi api;

    String did = "web:did:" + FAKER.internet().domainName();

    @BeforeAll
    static void setUpClass() {
        var apiClient = ClientUtils.createApiClient(API_URL, CLIENT_DID_WEB, TestKeyData.PRIVATE_KEY_P256);
        api = new RegistryApi(apiClient);
    }

    @Test
    void listParticipants() {

        assertThat(api.listParticipants())
                .noneSatisfy(p -> assertThat(p.getDid()).isEqualTo(did));

        api.addParticipant(did);

        assertThat(api.listParticipants())
                .anySatisfy(p -> assertThat(p.getDid()).isEqualTo(did));
    }
}
