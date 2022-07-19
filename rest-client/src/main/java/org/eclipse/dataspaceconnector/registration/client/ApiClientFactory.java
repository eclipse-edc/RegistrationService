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

import org.eclipse.dataspaceconnector.spi.iam.TokenParameters;
import org.eclipse.dataspaceconnector.spi.iam.TokenRepresentation;
import org.eclipse.dataspaceconnector.spi.result.Result;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * Factory class for {@link ApiClient}.
 */
public class ApiClientFactory {
    private ApiClientFactory() {
    }

    /**
     * Create a new instance of {@link ApiClient} configured to access the given URL.
     * <p>
     * Note that the type of {@code credentialsProvider} is modeled on the EDC {@code IdentityService} interface, for easier integration.
     *
     * @param baseUri             API base URL.
     * @param credentialsProvider Provider for client credential.
     * @return API client.
     */
    @NotNull
    public static ApiClient createApiClient(String baseUri, Function<TokenParameters, Result<TokenRepresentation>> credentialsProvider) {
        var apiClient = new ApiClient();
        apiClient.updateBaseUri(baseUri);
        apiClient.setRequestInterceptor(new JsonWebSignatureHeaderInterceptor(credentialsProvider, baseUri));
        return apiClient;
    }
}
