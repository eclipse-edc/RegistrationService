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

/**
 * This is a wrapper for a {@link Policy}. Every Dataspace may have a policy for onboarding participants into the
 * dataspace.
 */
public class DataspacePolicy {
    public static final String ONBOARDING_SCOPE = "PARTICIPANT_REGISTRATION";
    private final Policy dataspacePolicy;

    public DataspacePolicy(Policy dataspacePolicy) {
        this.dataspacePolicy = dataspacePolicy;
    }

    public Policy get() {
        return dataspacePolicy;
    }
}
