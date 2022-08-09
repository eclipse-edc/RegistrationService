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

package org.eclipse.dataspaceconnector.registration.authority.model;

import org.eclipse.dataspaceconnector.registration.authority.TestUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.eclipse.dataspaceconnector.registration.authority.model.ParticipantStatus.AUTHORIZED;
import static org.eclipse.dataspaceconnector.registration.authority.model.ParticipantStatus.AUTHORIZING;
import static org.eclipse.dataspaceconnector.registration.authority.model.ParticipantStatus.DENIED;
import static org.eclipse.dataspaceconnector.registration.authority.model.ParticipantStatus.ONBOARDING_INITIATED;

class ParticipantTest {

    @ParameterizedTest
    @MethodSource("allowedStatesForTransitioningToAuthorizing")
    void transitionAuthorizing_fromAllowedState(ParticipantStatus status) {
        var build = participantWithStatus(status);
        build.transitionAuthorizing();
        assertThat(build.getStatus()).isEqualTo(AUTHORIZING);
    }

    @ParameterizedTest
    @MethodSource("notAllowedStatesForTransitioningToAuthorizing")
    void transitionAuthorizing_fromNotAllowedState(ParticipantStatus status) {
        var build = participantWithStatus(status);
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(build::transitionAuthorizing);
    }

    @ParameterizedTest
    @MethodSource("allowedStatesForTransitioningToAuthorized")
    void transitionAuthorized_fromAllowedState(ParticipantStatus status) {
        var build = participantWithStatus(status);
        build.transitionAuthorized();
        assertThat(build.getStatus()).isEqualTo(AUTHORIZED);
    }

    private Participant participantWithStatus(ParticipantStatus status) {
        return TestUtils.createParticipant()
                .status(status)
                .build();
    }

    @ParameterizedTest
    @MethodSource("notAllowedStatesForTransitioningToAuthorized")
    void transitionAuthorized_fromNotAllowedState(ParticipantStatus status) {
        Participant build = participantWithStatus(status);
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(build::transitionAuthorized);
    }

    @ParameterizedTest
    @MethodSource("allowedStatesForTransitioningToDenied")
    void transitionDenied_fromAllowedState(ParticipantStatus status) {
        Participant build = participantWithStatus(status);
        build.transitionDenied();
        assertThat(build.getStatus()).isEqualTo(DENIED);
    }

    @ParameterizedTest
    @MethodSource("notAllowedStatesForTransitioningToDenied")
    void transitionDenied_fromNotAllowedState(ParticipantStatus status) {
        Participant build = participantWithStatus(status);
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(build::transitionDenied);
    }

    static List<ParticipantStatus> allowedStatesForTransitioningToAuthorizing() {
        return List.of(ONBOARDING_INITIATED);
    }

    static Stream<ParticipantStatus> notAllowedStatesForTransitioningToAuthorizing() {
        return statesNotIn(allowedStatesForTransitioningToAuthorizing());
    }

    static List<ParticipantStatus> allowedStatesForTransitioningToAuthorized() {
        return List.of(AUTHORIZING);
    }

    static Stream<ParticipantStatus> notAllowedStatesForTransitioningToAuthorized() {
        return statesNotIn(allowedStatesForTransitioningToAuthorized());
    }

    static List<ParticipantStatus> allowedStatesForTransitioningToDenied() {
        return List.of(AUTHORIZING);
    }

    static Stream<ParticipantStatus> notAllowedStatesForTransitioningToDenied() {
        return statesNotIn(allowedStatesForTransitioningToDenied());
    }

    static Stream<ParticipantStatus> statesNotIn(List<ParticipantStatus> allowed) {
        return Arrays.stream(ParticipantStatus.values()).filter(not(allowed::contains));
    }
}