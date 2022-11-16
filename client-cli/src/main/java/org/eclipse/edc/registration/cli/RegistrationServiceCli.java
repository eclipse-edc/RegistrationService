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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import okhttp3.OkHttpClient;
import org.eclipse.edc.iam.did.web.resolution.WebDidResolver;
import org.eclipse.edc.registration.client.api.RegistryApi;
import org.eclipse.edc.spi.monitor.ConsoleMonitor;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static org.eclipse.edc.registration.cli.ClientUtils.createApiClient;

@Command(name = "registration-service-cli", mixinStandardHelpOptions = true,
        description = "Client utility for MVD registration service.",
        subcommands = {
                ParticipantsCommand.class
        })
public class RegistrationServiceCli {

    static final ObjectMapper MAPPER = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(SerializationFeature.INDENT_OUTPUT);
    private final ConsoleMonitor monitor = new ConsoleMonitor();
    @CommandLine.Option(names = { "-d", "--dataspace-did" }, required = true, description = "Dataspace Authority DID.")
    String dataspaceDid;
    @CommandLine.Option(names = { "-c", "--client-did" }, required = true, description = "Client DID.")
    String clientDid;
    @CommandLine.Option(names = { "-k", "--private-key" }, required = true, description = "File containing the private key in PEM format")
    Path privateKeyFile;
    @CommandLine.Option(names = "--http-scheme", description = "Flag to create DID URLs with http instead of https scheme. Used for testing purposes.")
    boolean useHttpScheme;
    RegistryApi registryApiClient;

    public static void main(String... args) {
        CommandLine commandLine = getCommandLine();
        var exitCode = commandLine.execute(args);
        System.exit(exitCode);
    }

    public static CommandLine getCommandLine() {
        var command = new RegistrationServiceCli();
        return new CommandLine(command)
                .setExecutionStrategy(command::executionStrategy);
    }

    private int executionStrategy(CommandLine.ParseResult parseResult) {
        init(); // custom initialization to be done before executing any command or subcommand
        return new CommandLine.RunLast().execute(parseResult);
    }

    private void init() {
        String privateKeyData;
        try {
            privateKeyData = Files.readString(privateKeyFile);
        } catch (IOException e) {
            throw new RuntimeException("Error reading file " + privateKeyFile, e);
        }

        registryApiClient = new RegistryApi(createApiClient(registrationUrl(), clientDid, privateKeyData));
    }

    private String registrationUrl() {
        var didWebResolver = new WebDidResolver(httpClient(), !useHttpScheme, MAPPER, monitor);
        var urlResolver = new RegistrationUrlResolver(didWebResolver);
        var url = urlResolver.resolveUrl(dataspaceDid);
        if (url.failed()) {
            throw new CliException("Error resolving the registration url.");
        }
        return url.getContent();
    }

    @NotNull
    private OkHttpClient httpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }
}
