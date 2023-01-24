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

package org.eclipse.edc.registration.spi.registration;


import org.eclipse.edc.policy.engine.spi.PolicyScope;
import org.eclipse.edc.policy.model.Policy;
import org.eclipse.edc.runtime.metamodel.annotation.ExtensionPoint;

/**
 * This is a wrapper for a {@link Policy}. Every Dataspace may have its own policy for onboarding participants into the dataspace.
 */
@ExtensionPoint
@FunctionalInterface
public interface DataspaceRegistrationPolicy {
    @PolicyScope
    String PARTICIPANT_REGISTRATION_SCOPE = "dataspace.participant.registration";

    Policy get();
}
