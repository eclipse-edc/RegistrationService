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

package org.eclipse.edc.registration.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.edc.iam.did.crypto.JwtUtils;
import org.eclipse.edc.registration.client.ApiClientFactory;
import org.eclipse.edc.registration.client.RegistryApiClient;
import org.eclipse.edc.spi.iam.TokenParameters;
import org.eclipse.edc.spi.iam.TokenRepresentation;
import org.eclipse.edc.spi.monitor.ConsoleMonitor;
import org.eclipse.edc.spi.result.Result;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.io.IOException;
import java.time.Clock;
import java.util.Objects;
import java.util.function.Function;

import static org.eclipse.edc.registration.cli.RegistrationServiceCli.MAPPER;


public class ClientUtils {
    private ClientUtils() {
    }

    /**
     * Create a registration apiUrl API client configured to issue JWT tokens from the given issuer, signed by the given key.
     *
     * @param apiUrl         API base URL.
     * @param issuer         JWT token issuer.
     * @param privateKeyData JWT token signing key.
     * @return configured API client.
     */
    @NotNull
    public static RegistryApiClient createApiClient(String apiUrl, String issuer, String privateKeyData) {
        var privateKey = CryptoUtils.parseFromPemEncodedObjects(privateKeyData);

        Function<TokenParameters, Result<TokenRepresentation>> generatorFunction = parameters -> {
            var token = JwtUtils.create(
                    privateKey,
                    issuer,
                    issuer,
                    Objects.requireNonNull(parameters.getAudience(), "audience"),
                    Clock.systemUTC()).serialize();
            return Result.success(TokenRepresentation.Builder.newInstance().token(token).build());
        };
        // console monitor is fine here, since this is a CLI project
        return ApiClientFactory.createApiClient(apiUrl, generatorFunction, new ConsoleMonitor(), new ObjectMapper());

    }

    /**
     * Write response object to writer associated with CommandLine output.
     *
     * @param commandLine {@link CommandLine}
     * @param response    object to be written on output.
     * @throws IOException if fails to serialize response value as JSON output.
     */
    public static void writeToOutput(CommandLine commandLine, Object response) {
        var out = commandLine.getOut();
        try {
            MAPPER.writeValue(out, response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        out.println();
    }
}
