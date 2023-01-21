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

package org.eclipse.edc.registration.cli;

import org.eclipse.edc.registration.client.models.ParticipantDto;
import org.eclipse.edc.registration.client.models.ParticipantDto.StatusEnum;

import static java.lang.String.format;

public class TestUtils {

    private TestUtils() {
    }

    public static ParticipantDto createParticipantDto() {
        return new ParticipantDto()
                .did(format("did:web:%s", "test-domain"))
                .status(StatusEnum.ONBOARDED);
    }
}
