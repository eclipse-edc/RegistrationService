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

import org.eclipse.dataspaceconnector.registration.client.ApiClientFactory;
import org.eclipse.dataspaceconnector.registration.client.api.RegistryApi;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "registration-service-cli", mixinStandardHelpOptions = true,
        description = "Client utility for MVD registration service.",
        subcommands = {
                ParticipantsCommand.class
        })
public class RegistrationServiceCli {
    @CommandLine.Option(names = "-s", required = true, description = "Registration service URL", defaultValue = "http://localhost:8182/authority")
    String service;

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
        var apiClient = ApiClientFactory.createApiClient(service);
        registryApiClient = new RegistryApi(apiClient);

    }
}
