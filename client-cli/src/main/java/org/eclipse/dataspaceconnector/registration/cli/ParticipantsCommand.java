package org.eclipse.dataspaceconnector.registration.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "participants", mixinStandardHelpOptions = true,
        description = "Manage dataspace participants.",
        subcommands = {
                ListParticipantsCommand.class
        })
class ParticipantsCommand {
        @CommandLine.ParentCommand
        RegistrationServiceCli parent;
}
