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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.dataspaceconnector.registration.client.models.Participant;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

import java.util.concurrent.Callable;

@Command(name = "add", description = "Add a participant to dataspace")
class AddParticipantCommand implements Callable<Integer> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @ParentCommand
    private ParticipantsCommand command;

    @CommandLine.Option(names = "--request", required = true, description = "Add Participant Request as JSON")
    private String requestJson;

    @Override
    public Integer call() throws Exception {
        Participant participant = null;
        try {
            participant = MAPPER.readValue(requestJson, Participant.class);
        } catch (JsonProcessingException e) {
            throw new CliException("Error while processing request json.");
        }
        command.cli.registryApiClient.addParticipant(participant);

        return 0;
    }
}
