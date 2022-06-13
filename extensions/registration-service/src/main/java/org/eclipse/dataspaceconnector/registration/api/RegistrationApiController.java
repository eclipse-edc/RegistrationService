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

package org.eclipse.dataspaceconnector.registration.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import org.eclipse.dataspaceconnector.registration.api.model.Participant;

import java.util.List;

/**
 * Registration Service API controller to manage dataspace participants.
 */
@Tag(name = "Registry")
@Produces({"application/json"})
@Consumes({"application/json"})
@Path("/registry")
public class RegistrationApiController {

    private final RegistrationService service;

    /**
     * Constructs an instance of {@link RegistrationApiController}
     *
     * @param service service handling the registration service logic.
     */
    public RegistrationApiController(RegistrationService service) {
        this.service = service;
    }

    @Path("/participants")
    @GET
    @Operation(description = "Gets all dataspace participants.")
    @ApiResponse(description = "Dataspace participants.")
    public List<Participant> listParticipants() {
        return service.listParticipants();
    }

    @Path("/participant")
    @Operation(description = "Asynchronously request to add a dataspace participant.")
    @ApiResponse(responseCode = "204", description = "No content")
    @POST
    public void addParticipant(Participant participant) {
        service.addParticipant(participant);
    }
}
