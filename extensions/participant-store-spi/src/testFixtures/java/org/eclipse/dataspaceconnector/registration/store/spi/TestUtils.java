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
import org.eclipse.dataspaceconnector.registration.authority.model.ParticipantStatus;

import java.util.UUID;

public class TestUtils {

    private TestUtils() {
    }

    public static Participant.Builder createParticipant() {
        return Participant.Builder.newInstance()
                .id(UUID.randomUUID().toString())
                .did("some.test/url")
                .status(ParticipantStatus.AUTHORIZED);
    }
}
