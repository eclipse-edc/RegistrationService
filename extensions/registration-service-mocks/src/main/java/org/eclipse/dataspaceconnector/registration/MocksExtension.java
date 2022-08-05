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

/**
 * EDC extension to boot the services used by the Authority Service.
 */
public class MocksExtension implements ServiceExtension {

    @Override
    public void initialize(ServiceExtensionContext context) {
    }

    @Provider(isDefault = true)
    public CredentialsVerifier createRegionIsEuVerifier() {
        return participantDid -> Result.success(Map.of("region", "eu"));
    }

    @Provider(isDefault = true)
    public DataspacePolicyHolder createDataspacePolicy() {
        var p = Policy.Builder.newInstance().build();
        return new DataspacePolicyHolder(p);
    }
}
