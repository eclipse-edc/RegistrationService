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

package org.eclipse.dataspaceconnector.registration;

import org.eclipse.dataspaceconnector.iam.did.spi.credentials.CredentialsVerifier;
import org.eclipse.dataspaceconnector.policy.model.Policy;
import org.eclipse.dataspaceconnector.spi.result.Result;
import org.eclipse.dataspaceconnector.spi.system.Provider;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext;

import java.util.Map;
import java.util.UUID;

/**
 * EDC extension to boot the services used by the Authority Service.
 */
public class MocksExtension implements ServiceExtension {
    private static final String VERIFIABLE_CREDENTIAL_ID_KEY = "id";
    private static final String CREDENTIAL_SUBJECT_KEY = "credentialSubject";
    private static final String ISSUER_KEY = "iss";
    private static final String VERIFIABLE_CREDENTIALS_KEY = "vc";

    @Override
    public void initialize(ServiceExtensionContext context) {
    }

    @Provider(isDefault = true)
    public CredentialsVerifier createRegionIsEuVerifier() {

        var vcId = UUID.randomUUID().toString();
        return (participant) -> Result.success(Map.of(vcId,
                Map.of(VERIFIABLE_CREDENTIALS_KEY,
                        Map.of(CREDENTIAL_SUBJECT_KEY, "eu",
                                VERIFIABLE_CREDENTIAL_ID_KEY, vcId),
                        // issuer will be ignored when applying policies for now.
                        ISSUER_KEY, String.join("did:web:", UUID.randomUUID().toString()))));
    }

    @Provider(isDefault = true)
    public DataspacePolicy createDataspacePolicy() {
        var p = Policy.Builder.newInstance().build();
        return new DataspacePolicy(p);
    }
}
