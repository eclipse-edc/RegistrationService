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

package org.eclipse.edc.registration.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Participant onboarding status. Some internal statuses are mapping to more general statuses, to avoid leaking details about the registration process.
 */
@Schema(description = "Participant onboarding status")
public enum ParticipantStatusDto {
    ONBOARDING_IN_PROGRESS, // participant onboarding in progress
    ONBOARDED, // participant is fully onboarded
    DENIED, // participant onboarding request denied
}
