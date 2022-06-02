package org.eclipse.dataspaceconnector.registration.api;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@OpenAPIDefinition(
        info = @Info(
                title = "Eclipse Dataspace Connector Registration Service",
                version = "0.0.1"
        )
)
public class OpenApiCommon {
    private OpenApiCommon() {
    }
}
