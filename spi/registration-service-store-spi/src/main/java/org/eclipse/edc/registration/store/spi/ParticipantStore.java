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

package org.eclipse.edc.registration.store.spi;

import org.eclipse.edc.registration.spi.model.Participant;
import org.eclipse.edc.registration.spi.model.ParticipantStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public interface ParticipantStore {

    @Nullable Participant findByDid(String did);

    List<Participant> listParticipants();

    void save(Participant participant);

    Collection<Participant> listParticipantsWithStatus(ParticipantStatus state);
}
