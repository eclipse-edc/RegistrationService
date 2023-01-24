/*
 *  Copyright (c) 2023 Amadeus
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Amadeus - initial API and implementation
 *
 */

package org.eclipse.edc.registration.spi.registration;

import org.eclipse.edc.registration.spi.model.Participant;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Registration service for dataspace participants.
 */
public interface RegistrationService {

    /**
     * Find a participant by its DID.
     *
     * @param did DID of participant.
     * @return participant.
     */
    @Nullable
    Participant findByDid(String did);

    /**
     * List all dataspace participants.
     *
     * @return all participants.
     */
    List<Participant> listParticipants();

    /**
     * Add a participant to a dataspace.
     *
     * @param did the DID of the dataspace participant to add.
     */
    void addParticipant(String did);
}
