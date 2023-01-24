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

package org.eclipse.edc.registration.spi.model;

import java.util.Arrays;

public enum ParticipantStatus {
    ONBOARDING_INITIATED(0), // onboarding request received
    AUTHORIZING(100), // verifying participants credentials
    AUTHORIZED(200), // participant is authorized
    ONBOARDED(300), // participant is fully onboarded
    DENIED(400), // participant onboarding request denied
    FAILED(-1); // participant onboarding failed

    private final int code;


    ParticipantStatus(int code) {
        this.code = code;
    }

    public static ParticipantStatus from(int code) {
        return Arrays.stream(values()).filter(tps -> tps.code == code).findFirst().orElse(null);
    }

    public int code() {
        return code;
    }
}
