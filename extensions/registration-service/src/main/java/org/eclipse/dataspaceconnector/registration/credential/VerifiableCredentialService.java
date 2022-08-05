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

package org.eclipse.dataspaceconnector.registration.credential;

import org.eclipse.dataspaceconnector.registration.authority.model.Participant;
import org.eclipse.dataspaceconnector.spi.response.StatusResult;

/**
 * Generates dataspace membership verifiable credential, and pushes it to the participant's identity hub
 * resolved from its DID Document.
 */
public interface VerifiableCredentialService {
    StatusResult<Void> pushVerifiableCredential(Participant participant);
}
