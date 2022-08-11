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
import org.eclipse.dataspaceconnector.spi.policy.PolicyScope;

/**
 * This is a wrapper for a {@link Policy}. Every Dataspace must have a policy for onboarding participants into the
 * dataspace.
 */
public class DataspaceRegistrationPolicy {
    @PolicyScope
    public static final String PARTICIPANT_REGISTRATION_SCOPE = "dataspace.participant.registration";

    private final Policy policy;

    public DataspaceRegistrationPolicy(Policy policy) {
        this.policy = policy;
    }

    public Policy get() {
        return policy;
    }
}
