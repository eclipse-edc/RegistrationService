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

package org.eclipse.dataspaceconnector.registration.policy;

import com.github.javafaker.Faker;
import org.eclipse.dataspaceconnector.junit.extensions.EdcExtension;
import org.eclipse.dataspaceconnector.registration.DataspaceRegistrationPolicy;
import org.eclipse.dataspaceconnector.spi.agent.ParticipantAgent;
import org.eclipse.dataspaceconnector.spi.policy.PolicyEngine;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.dataspaceconnector.registration.DataspaceRegistrationPolicy.PARTICIPANT_REGISTRATION_SCOPE;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@ExtendWith(EdcExtension.class)
class GaiaxMemberDataspaceRegistrationPolicyExtensionTest {

    private static final Faker FAKER = new Faker();

    @ParameterizedTest
    @MethodSource("claimCases")
    void createDataspaceRegistrationPolicy(Map<String, Object> claims, boolean expected, PolicyEngine policyEngine, DataspaceRegistrationPolicy policy) {
        var agent = new ParticipantAgent(claims, Collections.emptyMap());

        var evaluationResult = policyEngine.evaluate(PARTICIPANT_REGISTRATION_SCOPE, policy.get(), agent);

        assertThat(evaluationResult.succeeded()).isEqualTo(expected);
    }

    private static Stream<Arguments> claimCases() {
        return Stream.of(
                arguments(claims(vc(subject(Map.of("gaiaXMember", "true")))), true),
                arguments(claims(vc(subject(Map.of("gaiaXMember", rnd())))), false),
                arguments(claims(vc(subject(Map.of("gaiaXMember", "true ")))), false),
                arguments(claims(vc(subject(Map.of("gaiaXMember", "")))), false),
                arguments(claims(vc(subject(Map.of("GaiaXMember", "true")))), false),
                arguments(claims(vc(subject(Map.of("gaiaXMember", "true", rnd(), rnd())))), true),
                arguments(claims(vc(subject(new Object()))), false),
                arguments(claims(vc(new Object())), false),
                arguments(claims(new Object()), false),
                arguments(claims(vc(subject(Map.of("gaiaXMember", "true"))), vc(subject(Map.of("gaiaXMember", "true")))), true),
                arguments(claims(vc(subject(Map.of("gaiaXMember", "true"))), vc(subject(Map.of("gaiaXMember", rnd())))), true),
                arguments(claims(vc(subject(Map.of("gaiaXMember", rnd()))), vc(subject(Map.of("gaiaXMember", "true")))), true),
                arguments(Map.of(), false)
        );
    }

    private static String rnd() {
        return FAKER.lorem().sentence();
    }

    private static Map<String, Object> claims(Object... claims) {
        return Arrays.stream(claims).collect(Collectors.toMap(e -> FAKER.internet().uuid(), e -> e));
    }

    private static Map<String, Object> vc(Object vc) {
        return Map.of("vc", vc);
    }

    private static Map<String, Object> subject(Object credentialSubject) {
        return Map.of("credentialSubject", credentialSubject);
    }
}
