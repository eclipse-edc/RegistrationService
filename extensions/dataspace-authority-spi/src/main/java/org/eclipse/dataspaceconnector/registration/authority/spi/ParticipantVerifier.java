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

package org.eclipse.dataspaceconnector.registration.authority.spi;

import org.eclipse.dataspaceconnector.spi.response.StatusResult;

/**
 * Verifier that fetches and evaluate verifiable credentials to
 * authorize dataspace membership.
 */
public interface ParticipantVerifier {
    StatusResult<Boolean> verifyCredentials(String did);
}
