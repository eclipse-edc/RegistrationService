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

import org.eclipse.dataspaceconnector.registration.api.model.Participant;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * In-memory store for dataspace participants.
 */
public class InMemoryParticipantStore {

    private final Map<String, Participant> participantStore = new LinkedHashMap<>();

    public List<Participant> listParticipants() {
        return new ArrayList<>(participantStore.values());
    }

    public void addParticipant(Participant participant) {
        participantStore.put(participant.getName(), participant);
    }
}
