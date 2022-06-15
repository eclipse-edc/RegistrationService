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
import org.eclipse.dataspaceconnector.registration.store.model.Participant;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryParticipantStoreTest {

    InMemoryParticipantStore store = new InMemoryParticipantStore();
    Participant participant1 = TestUtils.createParticipant().build();
    Participant participant1OtherEntry = TestUtils.createParticipant().name(participant1.getName()).build();
    Participant participant2 = TestUtils.createParticipant().build();

    @Test
    void addAndListParticipants() {
        assertThat(store.listParticipants()).isEmpty();

        store.addParticipant(participant1);
        assertThat(store.listParticipants()).containsOnly(participant1);
    }

    @Test
    void addAndListParticipants_removesDuplicates() {
        store.addParticipant(participant1);
        store.addParticipant(participant1OtherEntry);
        assertThat(store.listParticipants()).containsOnly(participant1OtherEntry);
    }

    @Test
    void addAndListParticipants_twoEntries() {
        store.addParticipant(participant1);
        store.addParticipant(participant2);
        assertThat(store.listParticipants()).containsOnly(participant1, participant2);
    }
}