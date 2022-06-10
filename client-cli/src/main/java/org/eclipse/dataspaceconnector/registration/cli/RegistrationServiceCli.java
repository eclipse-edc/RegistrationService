package org.eclipse.dataspaceconnector.registration.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "registration-service-cli", mixinStandardHelpOptions = true,
        description = "Client utility for MVD registration service.")
public class RegistrationServiceCli {
    @CommandLine.Option(names = "-s", required = true, description = "Registration service URL")
    String service;

    public static void main(String... args) {
        int exitCode = new CommandLine(new RegistrationServiceCli()).execute(args);
        System.exit(exitCode);
    }
}
