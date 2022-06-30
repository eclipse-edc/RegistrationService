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

package org.eclipse.dataspaceconnector.registration.store;

import org.eclipse.dataspaceconnector.registration.TestUtils;
import org.eclipse.dataspaceconnector.registration.authority.model.Participant;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.dataspaceconnector.registration.authority.model.ParticipantStatus.AUTHORIZED;
import static org.eclipse.dataspaceconnector.registration.authority.model.ParticipantStatus.AUTHORIZING;

class InMemoryParticipantStoreTest {

    InMemoryParticipantStore store = new InMemoryParticipantStore();
    Participant participant1 = TestUtils.createParticipant().build();
    Participant participant1OtherEntry = TestUtils.createParticipant().did(participant1.getDid()).build();
    Participant participant2 = TestUtils.createParticipant().build();

    @Test
    void saveAndListParticipants() {
        assertThat(store.listParticipants()).isEmpty();

        store.save(participant1);
        assertThat(store.listParticipants()).containsOnly(participant1);
    }

    @Test
    void saveAndListParticipants_removesDuplicates() {
        store.save(participant1);
        store.save(participant1OtherEntry);
        assertThat(store.listParticipants()).containsOnly(participant1OtherEntry);
    }

    @Test
    void saveAndListParticipants_twoEntries() {
        store.save(participant1);
        store.save(participant2);
        assertThat(store.listParticipants()).containsOnly(participant1, participant2);
    }

    @Test
    void listParticipantsWithStatus() {
        Participant participant01 = TestUtils.createParticipant().status(AUTHORIZED).build();
        Participant participant02 = TestUtils.createParticipant().status(AUTHORIZING).build();
        Participant participant03 = TestUtils.createParticipant().status(AUTHORIZED).build();
        store.save(participant01);
        store.save(participant02);
        store.save(participant03);
        assertThat(store.listParticipantsWithStatus(AUTHORIZED)).containsOnly(participant01, participant03);
    }
}
