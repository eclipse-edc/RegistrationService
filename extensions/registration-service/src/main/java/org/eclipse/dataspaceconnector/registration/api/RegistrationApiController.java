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
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import org.eclipse.dataspaceconnector.registration.model.ParticipantDto;
import org.eclipse.dataspaceconnector.registration.service.RegistrationService;

import java.util.List;
import java.util.Objects;

import static org.eclipse.dataspaceconnector.registration.auth.DidJwtAuthenticationFilter.CALLER_DID_HEADER;


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

    @GET
    @Path("/participant")
    @Operation(description = "Get a participant by caller DID.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Dataspace participant.",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ParticipantDto.class)
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Dataspace participant not found."
            )
    })
    public ParticipantDto getParticipant(@Context HttpHeaders headers) {
        var issuer = Objects.requireNonNull(headers.getHeaderString(CALLER_DID_HEADER));

        return service.findByDid(issuer);
    }

    @Path("/participants")
    @GET
    @Operation(description = "Gets all participants onboarded in the dataspace.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Dataspace participants.",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = ParticipantDto.class))
                            )
                    }
            )
    })
    public List<ParticipantDto> listParticipants() {
        return service.listOnboardedParticipants();
    }

    @Path("/participant")
    @Operation(description = "Asynchronously request to add a dataspace participant.")
    @ApiResponse(responseCode = "204", description = "No content")
    @POST
    public void addParticipant(@Context HttpHeaders headers) {
        var issuer = Objects.requireNonNull(headers.getHeaderString(CALLER_DID_HEADER));

        service.addParticipant(issuer);
    }
}
