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

package org.eclipse.dataspaceconnector.registration.client;

import com.github.javafaker.Faker;
import org.eclipse.dataspaceconnector.registration.client.models.Participant;

class IntegrationTestUtils {
    private IntegrationTestUtils() {
    }

    static final Faker FAKER = new Faker();

    public static Participant createParticipant() {
        return new Participant()
                .status(FAKER.options().option(Participant.StatusEnum.class))
                .name(FAKER.lorem().characters())
                .url(FAKER.internet().url())
                .addSupportedProtocolsItem(FAKER.lorem().word())
                .addSupportedProtocolsItem(FAKER.lorem().word());
    }
}