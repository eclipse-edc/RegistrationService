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

package org.eclipse.dataspaceconnector.registration.utils;

import org.eclipse.dataspaceconnector.iam.did.spi.document.DidDocument;
import org.eclipse.dataspaceconnector.spi.result.Result;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class for extracting information from DID Documents.
 */
public class DidDocumentUtils {
    private DidDocumentUtils() {
    }

    private static final String IDENTITY_HUB_SERVICE_TYPE = "IdentityHub";

    /**
     * Retrieve the first service of type @{code IdentityHub} from a @{link DidDocument}.
     *
     * @param didDocument DID Document
     * @return a successful result of a service of the required type is found, a failure result otherwise.
     */
    @NotNull
    public static Result<String> getIdentityHubBaseUrl(DidDocument didDocument) {
        var hubBaseUrl = didDocument
                .getService()
                .stream()
                .filter(s -> s.getType().equals(IDENTITY_HUB_SERVICE_TYPE))
                .findFirst();

        return hubBaseUrl.map(u -> Result.success(u.getServiceEndpoint()))
                .orElse(Result.failure("Failed getting Identity Hub URL"));
    }
}
