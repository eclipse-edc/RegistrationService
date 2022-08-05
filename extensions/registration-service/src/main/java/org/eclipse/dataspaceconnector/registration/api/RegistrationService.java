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

package org.eclipse.dataspaceconnector.registration.api;

import org.eclipse.dataspaceconnector.registration.authority.model.Participant;
import org.eclipse.dataspaceconnector.spi.result.Result;

import java.util.List;

/**
 * Service to list and add participants to the internal membership database.
 */
public interface RegistrationService {
    /**
     * List all participants that are in the dataspace.
     */
    List<Participant> listParticipants();

    /**
     * Adds a participant ("onboarding") to the database. For this to succeed, the following conditions must be true:
     * <ul>
     *     <li>DID must be resolvable</li>
     *     <li>DID must contain verifiable claims</li>
     *     <li>Claims must not be empty and satisfy the dataspace onboarding policy.</li>
     * </ul>
     *
     * @param did The DID of the participant.
     * @param idsUrl The IDS URL that should be registered for the participant
     * @return a success result, if the onboarding was successful, a failed result otherwise.
     * @see org.eclipse.dataspaceconnector.registration.DataspacePolicy
     */
    Result<Void> addParticipant(String did, String idsUrl);
}
