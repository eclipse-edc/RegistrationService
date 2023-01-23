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

package org.eclipse.edc.registration.store;

import org.eclipse.edc.registration.spi.model.Participant;
import org.eclipse.edc.registration.spi.model.ParticipantStatus;
import org.eclipse.edc.registration.store.spi.ParticipantStore;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory store for dataspace participants.
 */
public class InMemoryParticipantStore implements ParticipantStore {

    private final Map<String, Participant> storage = new ConcurrentHashMap<>();

    @Override
    public @Nullable Participant findByDid(String did) {
        return storage.get(did);
    }

    @Override
    public List<Participant> listParticipants() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public void save(Participant participant) {
        storage.put(participant.getDid(), participant);
    }

    @Override
    public Collection<Participant> listParticipantsWithStatus(ParticipantStatus status) {
        return storage.values().stream().filter(p -> p.getStatus() == status).collect(Collectors.toList());
    }
}
