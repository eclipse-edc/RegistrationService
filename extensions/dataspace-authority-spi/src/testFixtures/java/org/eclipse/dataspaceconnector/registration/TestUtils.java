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

package org.eclipse.dataspaceconnector.registration;

import com.github.javafaker.Faker;
import org.eclipse.dataspaceconnector.registration.authority.model.Participant;
import org.eclipse.dataspaceconnector.registration.authority.model.ParticipantStatus;

public class TestUtils {
    static final Faker FAKER = new Faker();

    private TestUtils() {
    }

    public static Participant.Builder createParticipant() {
        return Participant.Builder.newInstance()
                .did(FAKER.internet().url())
                .status(FAKER.options().option(ParticipantStatus.class));
    }
}