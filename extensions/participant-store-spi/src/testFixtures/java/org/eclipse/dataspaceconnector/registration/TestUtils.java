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
import org.eclipse.dataspaceconnector.registration.store.model.Participant;

public class TestUtils {
    private TestUtils() {
    }

    static final Faker FAKER = new Faker();

    public static Participant.Builder createParticipant() {
        return Participant.Builder.newInstance()
                .name(FAKER.lorem().characters())
                .url(FAKER.internet().url())
                .supportedProtocol(FAKER.lorem().word())
                .supportedProtocol(FAKER.lorem().word());
    }
}