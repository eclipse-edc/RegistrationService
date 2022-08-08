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

import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

import java.util.concurrent.Callable;

@Command(name = "add", description = "Add a participant to dataspace")
class AddParticipantCommand implements Callable<Integer> {

    @ParentCommand
    private ParticipantsCommand command;

    @Override
    public Integer call() {
        command.cli.registryApiClient.addParticipant(command.cli.clientDid);

        return 0;
    }
}
