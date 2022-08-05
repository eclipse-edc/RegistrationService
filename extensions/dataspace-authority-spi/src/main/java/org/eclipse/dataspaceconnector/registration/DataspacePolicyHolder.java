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


import org.eclipse.dataspaceconnector.policy.model.Policy;

public class DataspacePolicyHolder {
    private final Policy dataspacePolicy;

    public DataspacePolicyHolder(Policy dataspacePolicy) {
        this.dataspacePolicy = dataspacePolicy;
    }

    public Policy get() {
        return dataspacePolicy;
    }
}
