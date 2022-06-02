package org.eclipse.dataspaceconnector.registration.api.health;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

/**
 * Simple API controller to test service is available.
 */
@Tag(name = "Health")
@Produces({"application/json"})
@Consumes({"application/json"})
@Path("/health")
public class HealthApiController {

    @GET
    public Response healthy() {
        return Response.ok("healthy").build();
    }
}
