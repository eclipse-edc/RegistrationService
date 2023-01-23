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

package org.eclipse.edc.registration.spi.verifier;


import org.eclipse.edc.runtime.metamodel.annotation.ExtensionPoint;
import org.eclipse.edc.spi.response.StatusResult;

@FunctionalInterface
@ExtensionPoint
public interface OnboardingPolicyVerifier {

    /**
     * Determines is a participant is allowed to join the dataspace.
     *
     * @param did did of the participant.
     * @return true if the participant is allowed to join the dataspace.
     */
    StatusResult<Boolean> isOnboardingAllowed(String did);
}
