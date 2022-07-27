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

package org.eclipse.dataspaceconnector.registration.client;

class TestUtils {
    private TestUtils() {
    }

    /**
     * The DID that resolves to the sample DID Document for a client in docker compose (served by the nginx container)
     */
    static final String CLIENT_DID_WEB = "did:web:did-server:test-client";

    /**
     * The DID that resolves to the sample DID Document for the Dataspace Authority in docker compose (served by the nginx container).
     * Did web format reference: https://w3c-ccg.github.io/did-method-web/#create-register
     */
    static final String DATASPACE_DID_WEB = "did:web:localhost%3A8080:test-dataspace-authority";
}
