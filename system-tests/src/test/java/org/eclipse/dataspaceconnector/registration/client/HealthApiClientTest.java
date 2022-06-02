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

import org.eclipse.dataspaceconnector.registration.client.api.HealthApi;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.awaitility.Awaitility.await;

@IntegrationTest
public class HealthApiClientTest {
    static final String API_URL = "http://localhost:8181/api";

    ApiClient apiClient = ApiClientFactory.createApiClient(API_URL);
    HealthApi healthApi = new HealthApi(apiClient);

    @Test
    void healthy() {
        await().atMost(Duration.ofSeconds(30))
                .untilAsserted(healthApi::healthy);
    }
}
