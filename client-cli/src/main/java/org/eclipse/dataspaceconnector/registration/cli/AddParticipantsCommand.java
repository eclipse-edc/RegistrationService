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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.eclipse.dataspaceconnector.registration.client.models.Participant;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

import java.util.concurrent.Callable;

import static picocli.CommandLine.Parameters;

@Command(name = "add")
class AddParticipantsCommand implements Callable<Integer> {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    @ParentCommand
    private ParticipantsCommand parent;

    @Parameters(index = "0", arity = "1", paramLabel = "participantJson", description = "Participant JSON")
    private String participantJson;

    @Override
    public Integer call() throws Exception {
        var participant = MAPPER.readValue(participantJson, Participant.class);
        parent.parent.registryApiClient.addParticipant(participant);
        return 0;
    }
}
