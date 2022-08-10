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
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.Spec;

import java.util.concurrent.Callable;

import static org.eclipse.dataspaceconnector.registration.cli.ClientUtils.writeToOutput;

@Command(name = "list", description = "List dataspace participants")
class ListParticipantsCommand implements Callable<Integer> {

    @ParentCommand
    private ParticipantsCommand command;

    @Spec
    private CommandSpec spec;

    @Override
    public Integer call() throws Exception {
        writeToOutput(spec.commandLine(), command.cli.registryApiClient.listParticipants());
        return 0;
    }
}
