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

package org.eclipse.dataspaceconnector.registration.store.spi;

import org.eclipse.dataspaceconnector.registration.authority.model.Participant;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.dataspaceconnector.registration.authority.model.ParticipantStatus.AUTHORIZED;
import static org.eclipse.dataspaceconnector.registration.authority.model.ParticipantStatus.AUTHORIZING;
import static org.eclipse.dataspaceconnector.registration.authority.model.ParticipantStatus.ONBOARDED;
import static org.eclipse.dataspaceconnector.registration.store.spi.TestUtils.createParticipant;

public abstract class ParticipantStoreTestBase {


    Participant participant1 = createParticipant().build();
    Participant participant1OtherEntry = createParticipant().did(participant1.getDid()).id(participant1.getId()).build();
    Participant participant2 = createParticipant().did("some.test/url/1").build();

    @Test
    void listParticipants_empty() {
        assertThat(getStore().listParticipants()).isEmpty();
    }

    @Test
    void saveAndListParticipants() {
        getStore().save(participant1);
        assertThat(getStore().listParticipants()).usingRecursiveFieldByFieldElementComparator().containsOnly(participant1);
    }

    @Test
    void saveAndListParticipants_removesDuplicates() {
        getStore().save(participant1);
        getStore().save(participant1OtherEntry);
        assertThat(getStore().listParticipants()).usingRecursiveFieldByFieldElementComparator().containsOnly(participant1OtherEntry);
    }

    @Test
    void saveAndListParticipants_twoEntries() {
        getStore().save(participant1);
        getStore().save(participant2);
        assertThat(getStore().listParticipants())
                .usingRecursiveFieldByFieldElementComparator()
                .containsOnly(participant1, participant2);
    }

    @Test
    void listParticipantsWithStatus() {
        Participant participant01 = createParticipant().status(AUTHORIZED).build();
        Participant participant02 = createParticipant().did("some.test/url/2").status(AUTHORIZING).build();
        Participant participant03 = createParticipant().did("some.test/url/3").status(AUTHORIZED).build();
        getStore().save(participant01);
        getStore().save(participant02);
        getStore().save(participant03);
        assertThat(getStore().listParticipantsWithStatus(AUTHORIZED))
                .usingRecursiveFieldByFieldElementComparator()
                .containsOnly(participant01, participant03);
    }

    @Test
    void findByDid_null() {
        assertThat(getStore().findByDid(participant1.getDid())).isNull();
    }

    @Test
    void saveAndFindByDid() {
        getStore().save(participant1);

        var participant = getStore().findByDid(participant1.getDid());
        assertThat(participant).usingRecursiveComparison().isEqualTo(participant1);
    }

    @Test
    void verify_shouldUpdateTransition() {
        var participant = createParticipant().status(AUTHORIZED).build();
        getStore().save(participant);

        participant.transitionOnboarded();
        getStore().save(participant);

        var participant1 = getStore().findByDid(participant.getDid());
        assertThat(participant1).extracting(Participant::getStatus).isEqualTo(ONBOARDED);

    }

    protected abstract ParticipantStore getStore();
}
