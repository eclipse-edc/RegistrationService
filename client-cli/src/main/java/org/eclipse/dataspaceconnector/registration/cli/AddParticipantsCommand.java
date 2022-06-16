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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.dataspaceconnector.registration.client.models.Participant;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

@Command(name = "add")
class AddParticipantsCommand implements Callable<Integer> {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final static Logger LOGGER = Logger.getLogger(AddParticipantsCommand.class.getName());

    @ParentCommand
    private ParticipantsCommand parent;

    @CommandLine.Option(names = "--request", required = true, description = "Add Participant Request as JSON")
    private String requestJson;

    @Override
    public Integer call() throws Exception {
        Participant participant = null;
        try {
            participant = MAPPER.readValue(requestJson, Participant.class);
        } catch (JsonProcessingException e) {
            LOGGER.log(Level.SEVERE, "Error while processing request json", e);
            throw new CliException("Error while processing request json.");
        }
        parent.parent.registryApiClient.addParticipant(participant);

        return 0;
    }
}
