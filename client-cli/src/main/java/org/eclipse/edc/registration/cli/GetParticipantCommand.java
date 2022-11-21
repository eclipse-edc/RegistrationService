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

import org.eclipse.edc.registration.client.ApiException;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

import java.util.concurrent.Callable;

import static org.eclipse.edc.registration.cli.ClientUtils.writeToOutput;
import static picocli.CommandLine.Model.CommandSpec;
import static picocli.CommandLine.Spec;

@Command(name = "get", description = "Get participant by caller DID")
class GetParticipantCommand implements Callable<Integer> {

    @ParentCommand
    private ParticipantsCommand command;

    @Spec
    private CommandSpec spec;

    @Override
    public Integer call() throws Exception {
        try {
            writeToOutput(spec.commandLine(), command.cli.registryApiClient.getParticipant());
            return 0;
        } catch (ApiException ex) {
            throw new CliException("Error occurred.", ex);
        }
    }
}
