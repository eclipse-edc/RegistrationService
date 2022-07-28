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

import org.eclipse.dataspaceconnector.iam.did.spi.document.DidDocument;
import org.eclipse.dataspaceconnector.iam.did.spi.document.Service;
import org.eclipse.dataspaceconnector.iam.did.spi.resolution.DidResolver;
import org.eclipse.dataspaceconnector.spi.result.Result;
import org.jetbrains.annotations.NotNull;

/**
 * Resolves the registration url from the DID.
 */
public class RegistrationUrlResolver {

    public static final String REGISTRATION_URL = "RegistrationUrl";

    /**
     * Resolves the DID document from did:web.
     */
    private final DidResolver resolver;

    public RegistrationUrlResolver(DidResolver resolver) {
        this.resolver = resolver;
    }

    @NotNull
    public Result<String> resolveUrl(String did) {
        Result<DidDocument> didDocument = resolver.resolve(did);
        if (didDocument.failed()) {
            throw new CliException("Error resolving the DID " + did);
        }
        return didDocument.getContent().getService()
                .stream()
                .filter(service -> service.getType().equals(REGISTRATION_URL))
                .map(Service::getServiceEndpoint)
                .findFirst()
                .map(Result::success)
                .orElse(Result.failure("Error resolving service endpoint from DID Document for " + did));
    }

}
