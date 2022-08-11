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

import org.eclipse.dataspaceconnector.registration.DataspaceRegistrationPolicy;
import org.eclipse.dataspaceconnector.registration.authority.spi.ParticipantVerifier;
import org.eclipse.dataspaceconnector.spi.agent.ParticipantAgent;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.policy.PolicyEngine;
import org.eclipse.dataspaceconnector.spi.response.StatusResult;
import org.eclipse.dataspaceconnector.spi.result.Result;

import java.util.Collections;
import java.util.Map;

import static org.eclipse.dataspaceconnector.registration.DataspaceRegistrationPolicy.PARTICIPANT_REGISTRATION_SCOPE;

public class DefaultParticipantVerifier implements ParticipantVerifier {
    private final Monitor monitor;
    private final PolicyEngine policyEngine;
    private final DataspaceRegistrationPolicy dataspaceRegistrationPolicy;

    public DefaultParticipantVerifier(Monitor monitor, PolicyEngine policyEngine, DataspaceRegistrationPolicy dataspaceRegistrationPolicy) {
        this.monitor = monitor;
        this.policyEngine = policyEngine;
        this.dataspaceRegistrationPolicy = dataspaceRegistrationPolicy;
    }

    @Override
    public StatusResult<Boolean> applyOnboardingPolicy(String participantDid) {
        var claimsResult = Result.success(Map.<String, Object>of("gaiaXMember", "true")); // TODO retrieve real credentials

        var agent = new ParticipantAgent(claimsResult.getContent(), Collections.emptyMap());

        var evaluationResult = policyEngine.evaluate(PARTICIPANT_REGISTRATION_SCOPE, dataspaceRegistrationPolicy.get(), agent);
        var policyResult = evaluationResult.succeeded();

        monitor.debug(() -> "Policy evaluation result: " + policyResult);

        return StatusResult.success(policyResult);
    }
}
