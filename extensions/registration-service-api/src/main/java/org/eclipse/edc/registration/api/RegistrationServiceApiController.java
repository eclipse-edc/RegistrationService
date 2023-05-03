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

package org.eclipse.edc.registration.api;

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
import jakarta.ws.rs.core.MediaType;
import org.eclipse.edc.registration.model.ParticipantDto;
import org.eclipse.edc.registration.spi.model.Participant;
import org.eclipse.edc.registration.spi.registration.RegistrationService;
import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.spi.result.Result;
import org.eclipse.edc.transform.spi.TypeTransformerRegistry;
import org.eclipse.edc.web.spi.exception.ObjectNotFoundException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.eclipse.edc.registration.auth.DidJwtAuthenticationFilter.CALLER_DID_HEADER;


/**
 * Registration Service API controller to manage dataspace participants.
 */
@Tag(name = "Registry")
@Produces({ MediaType.APPLICATION_JSON })
@Consumes({ MediaType.APPLICATION_JSON })
@Path("/registry")
public class RegistrationServiceApiController {

    private final RegistrationService service;
    private final TypeTransformerRegistry transformerRegistry;

    /**
     * Constructs an instance of {@link RegistrationServiceApiController}
     *
     * @param service             service handling the registration service logic.
     * @param transformerRegistry dto transformer registry
     */
    public RegistrationServiceApiController(RegistrationService service, TypeTransformerRegistry transformerRegistry) {
        this.service = service;
        this.transformerRegistry = transformerRegistry;
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
        var participant = Optional.ofNullable(service.findByDid(issuer))
                .orElseThrow(() -> new ObjectNotFoundException(Participant.class, issuer));

        var result = transformerRegistry.transform(participant, ParticipantDto.class);
        if (result.failed()) {
            throw new EdcException(result.getFailureDetail());
        }
        return result.getContent();
    }

    @Path("/participants")
    @GET
    @Operation(description = "Gets all dataspace participants.")
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
        return service.listParticipants().stream()
                .map(participant -> transformerRegistry.transform(participant, ParticipantDto.class))
                .filter(Result::succeeded)
                .map(Result::getContent)
                .collect(Collectors.toList());
    }

    @Path("/participant")
    @POST
    @Operation(description = "Asynchronously request to add a dataspace participant.")
    @ApiResponse(responseCode = "204", description = "No content")
    public void addParticipant(@Context HttpHeaders headers) {
        var issuer = Objects.requireNonNull(headers.getHeaderString(CALLER_DID_HEADER));

        service.addParticipant(issuer);
    }
}
