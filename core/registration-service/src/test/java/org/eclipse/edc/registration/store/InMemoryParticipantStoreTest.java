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

import org.eclipse.edc.registration.store.spi.ParticipantStore;
import org.eclipse.edc.registration.store.spi.ParticipantStoreTestBase;
import org.junit.jupiter.api.BeforeEach;


class InMemoryParticipantStoreTest extends ParticipantStoreTestBase {

    private InMemoryParticipantStore store;

    @BeforeEach
    void setup() {
        store = new InMemoryParticipantStore();
    }
    
    @Override
    protected ParticipantStore getStore() {
        return store;
    }
}
