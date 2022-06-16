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

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "participants", mixinStandardHelpOptions = true,
        description = "Manage dataspace participants.",
        subcommands = {
                ListParticipantsCommand.class,
                AddParticipantsCommand.class
        })
class ParticipantsCommand {
    @CommandLine.ParentCommand
    RegistrationServiceCli parent;
}
