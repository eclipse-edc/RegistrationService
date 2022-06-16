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
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.dataspaceconnector.registration.client.models.Participant;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.Spec;

import java.util.concurrent.Callable;

@Command(name = "add")
class AddParticipantsCommand implements Callable<Integer> {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);

    @ParentCommand
    private ParticipantsCommand parent;

    @Spec
    private CommandSpec spec;

    @CommandLine.Option(names = "--request", required = true, description = "Add Participant Request in JSON")
    private String requestJson;

    @Override
    public Integer call() throws Exception {
        var participant = MAPPER.readValue(requestJson, Participant.class);
        parent.parent.registryApiClient.addParticipant(participant);
//        var out = spec.commandLine().getOut();
//        MAPPER.writeValue(out, participant.getName());
//        MAPPER.writeValue(out, participant.getUrl());
//        MAPPER.writeValue(out, participant.getSupportedProtocols());
//        out.println();
        return 0;
    }
}
