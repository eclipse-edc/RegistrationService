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

package org.eclipse.edc.registration.spi.service;

import org.eclipse.edc.registration.spi.model.Participant;
import org.eclipse.edc.runtime.metamodel.annotation.ExtensionPoint;
import org.eclipse.edc.spi.response.StatusResult;

@FunctionalInterface
@ExtensionPoint
public interface VerifiableCredentialService {

    /**
     * Generates dataspace membership Verifiable Credential (VC) and pushes it to the participant's
     * Identity Hub (resolved from its DID Document).
     *
     * @param participant participant
     * @return success if VC pushed successfully to participant Identity Hub.
     */
    StatusResult<Void> pushVerifiableCredential(Participant participant);
}
