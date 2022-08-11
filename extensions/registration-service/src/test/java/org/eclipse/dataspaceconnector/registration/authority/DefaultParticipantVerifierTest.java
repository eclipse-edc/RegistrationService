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

package org.eclipse.dataspaceconnector.registration.authority;

import com.github.javafaker.Faker;
import org.eclipse.dataspaceconnector.policy.model.Policy;
import org.eclipse.dataspaceconnector.registration.DataspaceRegistrationPolicy;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.policy.PolicyEngine;
import org.eclipse.dataspaceconnector.spi.result.Result;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.dataspaceconnector.registration.DataspaceRegistrationPolicy.PARTICIPANT_REGISTRATION_SCOPE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DefaultParticipantVerifierTest {
    static final Faker FAKER = new Faker();

    String participantDid = FAKER.internet().url();
    String failure = FAKER.lorem().sentence();
    PolicyEngine policyEngine = mock(PolicyEngine.class);
    Policy policy = mock(Policy.class);
    Policy policyResult = mock(Policy.class);
    Monitor monitor = mock(Monitor.class);
    DataspaceRegistrationPolicy dataspaceRegistrationPolicy = new DataspaceRegistrationPolicy(policy);
    DefaultParticipantVerifier service = new DefaultParticipantVerifier(monitor, policyEngine, dataspaceRegistrationPolicy);

    @Test
    void applyOnboardingPolicy_success() {
        when(policyEngine.evaluate(eq(PARTICIPANT_REGISTRATION_SCOPE), eq(policy), argThat(a ->
                Map.of("gaiaXMember", "true").equals(a.getClaims()) &&
                        Map.of().equals(a.getAttributes()))))
                .thenReturn(Result.success(policyResult));

        var result = service.applyOnboardingPolicy(participantDid);

        assertThat(result.succeeded()).isTrue();
        assertThat(result.getContent()).isTrue();
    }

    @Test
    void applyOnboardingPolicy_failure() {
        when(policyEngine.evaluate(any(), any(), any()))
                .thenReturn(Result.failure(failure));

        var result = service.applyOnboardingPolicy(participantDid);

        assertThat(result.succeeded()).isTrue();
        assertThat(result.getContent()).isFalse();
    }
}