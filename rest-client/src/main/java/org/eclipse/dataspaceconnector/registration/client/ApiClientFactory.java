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

import org.eclipse.dataspaceconnector.runtime.metamodel.annotation.EdcSetting;
import org.eclipse.dataspaceconnector.spi.iam.TokenParameters;
import org.eclipse.dataspaceconnector.spi.iam.TokenRepresentation;
import org.eclipse.dataspaceconnector.spi.result.Result;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.function.Function;

import static org.eclipse.dataspaceconnector.common.configuration.ConfigurationFunctions.propOrEnv;

/**
 * Factory class for {@link ApiClient}.
 */
public class ApiClientFactory {

    @EdcSetting(type = "integer", value = "Rest api client connect timeout")
    private static final String API_CLIENT_CONNECT_TIMEOUT = "api.client.connect.timeout";
    @EdcSetting(type = "integer", value = "Rest api client read timeout")
    private static final String API_CLIENT_READ_TIMEOUT = "api.client.read.timeout";

    private ApiClientFactory() {
    }

    /**
     * Create a new instance of {@link ApiClient} configured to access the given URL.
     * <p>
     * Configured with connectTimeout (default is 30 seconds) and readTimeout (default is 60 seconds).
     * Note that the type of {@code credentialsProvider} is modeled on the EDC {@code IdentityService} interface, for easier integration.
     *
     * @param baseUri             API base URL.
     * @param credentialsProvider Provider for client credential.
     * @return API client.
     */
    @NotNull
    public static ApiClient createApiClient(String baseUri, Function<TokenParameters, Result<TokenRepresentation>> credentialsProvider) {
        var apiClient = new ApiClient();
        var connectTimeout = Integer.parseInt(propOrEnv(API_CLIENT_CONNECT_TIMEOUT, "30"));
        var readTimeout = Integer.parseInt(propOrEnv(API_CLIENT_READ_TIMEOUT, "60"));

        apiClient.setHttpClientBuilder(
                apiClient.createDefaultHttpClientBuilder()
                        .connectTimeout(Duration.ofSeconds(connectTimeout))
        );
        apiClient.setReadTimeout(Duration.ofSeconds(readTimeout));
        apiClient.updateBaseUri(baseUri);
        apiClient.setRequestInterceptor(new JsonWebSignatureHeaderInterceptor(credentialsProvider, baseUri));
        return apiClient;
    }
}
