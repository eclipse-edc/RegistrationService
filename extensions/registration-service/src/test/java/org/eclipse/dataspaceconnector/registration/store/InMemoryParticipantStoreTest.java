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

import org.eclipse.dataspaceconnector.registration.authority.model.Participant;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.dataspaceconnector.registration.authority.TestUtils.createParticipant;
import static org.eclipse.dataspaceconnector.registration.authority.model.ParticipantStatus.AUTHORIZED;
import static org.eclipse.dataspaceconnector.registration.authority.model.ParticipantStatus.AUTHORIZING;

class InMemoryParticipantStoreTest {

    InMemoryParticipantStore store = new InMemoryParticipantStore();
    Participant participant1 = createParticipant().build();
    Participant participant1OtherEntry = createParticipant().did(participant1.getDid()).build();
    Participant participant2 = createParticipant().build();

    @Test
    void listParticipants_empty() {
        assertThat(store.listParticipants()).isEmpty();
    }

    @Test
    void saveAndListParticipants() {
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
        assertThat(store.listParticipants())
                .usingRecursiveFieldByFieldElementComparator()
                .containsOnly(participant1, participant2);
    }

    @Test
    void listParticipantsWithStatus() {
        Participant participant01 = createParticipant().status(AUTHORIZED).build();
        Participant participant02 = createParticipant().status(AUTHORIZING).build();
        Participant participant03 = createParticipant().status(AUTHORIZED).build();
        store.save(participant01);
        store.save(participant02);
        store.save(participant03);
        assertThat(store.listParticipantsWithStatus(AUTHORIZED))
                .usingRecursiveFieldByFieldElementComparator()
                .containsOnly(participant01, participant03);
    }

    @Test
    void findByDid_null() {
        assertThat(store.findByDid(participant1.getDid())).isNull();
    }

    @Test
    void saveAndFindByDid() {
        store.save(participant1);

        var participant = store.findByDid(participant1.getDid());
        assertThat(participant).isEqualTo(participant1);
    }
}
