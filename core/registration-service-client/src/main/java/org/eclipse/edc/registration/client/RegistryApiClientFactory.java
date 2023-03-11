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
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - refactoring
 *
 */

package org.eclipse.edc.registration.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.failsafe.RetryPolicy;
import okhttp3.OkHttpClient;
import org.eclipse.edc.connector.core.base.EdcHttpClientImpl;
import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.spi.iam.TokenParameters;
import org.eclipse.edc.spi.iam.TokenRepresentation;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.result.Result;

import java.time.Duration;
import java.util.function.Function;

import static org.eclipse.edc.util.configuration.ConfigurationFunctions.propOrEnv;

/**
 * Factory class for {@link RegistryApiClient}.
 */
public class RegistryApiClientFactory {

    @Setting(type = "integer", value = "Rest api client connect timeout")
    private static final String API_CLIENT_CONNECT_TIMEOUT = "api.client.connect.timeout";
    @Setting(type = "integer", value = "Rest api client read timeout")
    private static final String API_CLIENT_READ_TIMEOUT = "api.client.read.timeout";

    private RegistryApiClientFactory() {
    }

    /**
     * Create a new instance of {@link RegistryApiClient} configured to access the given URL.
     * <p>
     * Configured with connectTimeout (default is 30 seconds) and readTimeout (default is 60 seconds).
     * Note that the type of {@code credentialsProvider} is modeled on the EDC {@code IdentityService} interface, for easier integration.
     *
     * @param baseUri             API base URL.
     * @param credentialsProvider Provider for client credential.
     * @return API client.
     */
    public static RegistryApiClient createApiClient(String baseUri, Function<TokenParameters, Result<TokenRepresentation>> credentialsProvider, Monitor monitor, ObjectMapper objectMapper) {
        var connectTimeout = Duration.ofSeconds(Integer.parseInt(propOrEnv(API_CLIENT_CONNECT_TIMEOUT, "30")));
        var readTimeout = Duration.ofSeconds(Integer.parseInt(propOrEnv(API_CLIENT_READ_TIMEOUT, "60")));

        var okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(connectTimeout)
                .readTimeout(readTimeout)
                .addInterceptor(new JsonWebSignatureHeaderInterceptor(credentialsProvider, baseUri))
                .build();

        var edcClient = new EdcHttpClientImpl(okHttpClient, RetryPolicy.ofDefaults(), monitor);

        return new RegistryApiClientImpl(edcClient, baseUri, objectMapper);
    }
}
