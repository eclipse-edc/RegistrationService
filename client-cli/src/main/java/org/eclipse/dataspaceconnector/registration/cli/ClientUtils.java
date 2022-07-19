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

package org.eclipse.dataspaceconnector.registration.cli;

import org.eclipse.dataspaceconnector.iam.did.crypto.credentials.VerifiableCredentialFactory;
import org.eclipse.dataspaceconnector.registration.client.ApiClient;
import org.eclipse.dataspaceconnector.registration.client.ApiClientFactory;
import org.eclipse.dataspaceconnector.spi.iam.TokenRepresentation;
import org.eclipse.dataspaceconnector.spi.result.Result;
import org.jetbrains.annotations.NotNull;

import java.time.Clock;
import java.util.Objects;


public class ClientUtils {
    private ClientUtils() {
    }

    /**
     * Create a registration apiUrl API client configured to issue JWT tokens from the given issuer, signed by the given key.
     *
     * @param apiUrl API base URL.
     * @param issuer JWT token issuer.
     * @param privateKeyData JWT token signing key.
     * @return configured API client.
     */
    @NotNull
    public static ApiClient createApiClient(String apiUrl, String issuer, String privateKeyData) {
        var privateKey = CryptoUtils.parseFromPemEncodedObjects(privateKeyData);

        return ApiClientFactory.createApiClient(apiUrl, parameters -> {
            var token = VerifiableCredentialFactory.create(
                    privateKey,
                    issuer,
                    Objects.requireNonNull(parameters.getAudience(), "audience"),
                    Clock.systemUTC()).serialize();
            return Result.success(TokenRepresentation.Builder.newInstance().token(token).build());
        });
    }
}
