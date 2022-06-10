package org.eclipse.dataspaceconnector.registration.cli;

import org.eclipse.dataspaceconnector.registration.client.ApiClient;
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
    @CommandLine.Option(names = "-s", required = true, description = "Registration service URL", defaultValue = "http://localhost:8181/api")
    String service;

    RegistryApi registryApiClient;

    public static void main(String... args) {
        var command = new RegistrationServiceCli();
        var exitCode = new CommandLine(command)
                .setExecutionStrategy(command::executionStrategy)
                .execute(args);
        System.exit(exitCode);
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
