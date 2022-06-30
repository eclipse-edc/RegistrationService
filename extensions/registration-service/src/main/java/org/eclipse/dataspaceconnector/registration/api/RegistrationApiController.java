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
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import org.eclipse.dataspaceconnector.registration.authority.model.Participant;

import java.util.List;

/**
 * Registration Service API controller to manage dataspace participants.
 */
@Tag(name = "Registry")
@Produces({ "application/json" })
@Consumes({ "application/json" })
@Path("/registry")
public class RegistrationApiController {

    /**
     * A IDS URL (this will be removed in https://github.com/agera-edc/MinimumViableDataspace/issues/174)
     */
    private static final String TEMPORARY_IDS_URL_HEADER = "IdsUrl";

    /**
     * A DID that identifies the caller (in the next PR to the same branch, this will be removed from operation parameters and extracted from the passed JWT. This is only here as a stopgap to make PRs smaller)
     */
    private static final String CALLER_DID_HEADER = "CallerDid";

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
    public void addParticipant(
            @HeaderParam(TEMPORARY_IDS_URL_HEADER) String idsUrl,
            @HeaderParam(CALLER_DID_HEADER) String did) {
        service.addParticipant(did, idsUrl);
    }
}
