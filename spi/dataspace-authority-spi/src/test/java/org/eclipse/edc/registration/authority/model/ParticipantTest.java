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

package org.eclipse.edc.registration.authority.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.edc.registration.authority.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.eclipse.edc.registration.authority.model.ParticipantStatus.AUTHORIZED;
import static org.eclipse.edc.registration.authority.model.ParticipantStatus.AUTHORIZING;
import static org.eclipse.edc.registration.authority.model.ParticipantStatus.DENIED;
import static org.eclipse.edc.registration.authority.model.ParticipantStatus.ONBOARDED;
import static org.eclipse.edc.registration.authority.model.ParticipantStatus.ONBOARDING_INITIATED;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ParticipantTest {


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

    @Test
    void copy_Participant() {
        var participant = TestUtils.createParticipant().build();
        var copiedParticipant = participant.copy();

        assertThat(participant).isNotSameAs(copiedParticipant);
        assertThat(participant).usingRecursiveComparison().isEqualTo(copiedParticipant);
    }

    @Test
    void verifySerDes() throws JsonProcessingException {
        var mapper = new ObjectMapper();
        var participant = Participant.Builder.newInstance()
                .did("some:did")
                .traceContext(Map.of("key1", "value1"))
                .status(ONBOARDED)
                .id(UUID.randomUUID().toString())
                .build();

        var json = mapper.writeValueAsString(participant);

        assertNotNull(json);

        var deser = mapper.readValue(json, Participant.class);
        assertThat(deser).usingRecursiveComparison().isEqualTo(participant);
    }

    private Participant participantWithStatus(ParticipantStatus status) {
        return TestUtils.createParticipant()
                .status(status)
                .build();
    }
}
