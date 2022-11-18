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

package org.eclipse.edc.registration.policy;

import org.eclipse.edc.policy.engine.spi.PolicyContext;
import org.eclipse.edc.policy.engine.spi.PolicyEngine;
import org.eclipse.edc.policy.engine.spi.RuleBindingRegistry;
import org.eclipse.edc.policy.model.AtomicConstraint;
import org.eclipse.edc.policy.model.LiteralExpression;
import org.eclipse.edc.policy.model.Operator;
import org.eclipse.edc.policy.model.Permission;
import org.eclipse.edc.policy.model.Policy;
import org.eclipse.edc.registration.DataspaceRegistrationPolicy;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Provider;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.system.ServiceExtension;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import static java.lang.String.format;
import static org.eclipse.edc.policy.model.Operator.EQ;
import static org.eclipse.edc.registration.DataspaceRegistrationPolicy.PARTICIPANT_REGISTRATION_SCOPE;

/**
 * EDC extension to create a policy that accepts participants with a GAIA-X membership credential.
 */
public class GaiaxMemberDataspaceRegistrationPolicyExtension implements ServiceExtension {
    public static final String CREDENTIAL_VALUE = "true";
    private static final String CREDENTIAL_NAME = "gaiaXMember";
    public static final String RULE_TYPE = CREDENTIAL_NAME;
    @Inject
    private Monitor monitor;

    @Inject
    private RuleBindingRegistry ruleBindingRegistry;

    @Inject
    private PolicyEngine policyEngine;

    /**
     * Registers a {@link Policy} and an evaluation function that checks that "gaiaXMember EQ
     * true". The rule type {@link #RULE_TYPE} is bound to the scope
     * {@link DataspaceRegistrationPolicy#PARTICIPANT_REGISTRATION_SCOPE}.
     */
    @Provider
    public DataspaceRegistrationPolicy createDataspaceRegistrationPolicy() {

        var constraint = AtomicConstraint.Builder.newInstance().leftExpression(new LiteralExpression(CREDENTIAL_NAME))
                .operator(EQ)
                .rightExpression(new LiteralExpression(CREDENTIAL_VALUE)).build();
        var permission = Permission.Builder.newInstance().constraint(constraint).build();
        var policy = Policy.Builder.newInstance()
                .permission(permission).build();
        ruleBindingRegistry.bind(RULE_TYPE, PARTICIPANT_REGISTRATION_SCOPE);
        policyEngine.registerFunction(PARTICIPANT_REGISTRATION_SCOPE, Permission.class, RULE_TYPE, this::evaluate);
        return new DataspaceRegistrationPolicy(policy);
    }

    private boolean evaluate(Operator operator, Object rightOperand, Permission rule, PolicyContext context) {
        monitor.debug(() -> format("Credentials %s", context.getParticipantAgent().getClaims()));
        // Order map by key (Verifiable Credential ID), to achieve deterministic output
        var claims = new TreeMap<>(context.getParticipantAgent().getClaims());
        for (var claim : claims.values()) {
            if (!(claim instanceof Map)) {
                monitor.warning(() -> "Ignoring claim that is not in Map format");
                continue;
            }
            var vc = ((Map<?, ?>) claim).get("vc");
            if (!(vc instanceof Map)) {
                monitor.warning(() -> "Ignoring claim that does not have a 'vc' entry in Map format");
                continue;
            }
            var subject = ((Map<?, ?>) vc).get("credentialSubject");
            if (!(subject instanceof Map)) {
                monitor.warning(() -> "Ignoring claim that does not have a 'vc' entry with a 'credentialSubject' entry in Map format");
                continue;
            }
            var subjectMap = (Map<?, ?>) subject;
            var value = subjectMap.get(CREDENTIAL_NAME);
            if (operator == EQ && Objects.equals(value, rightOperand)) {
                return true;
            }
        }
        return false;
    }
}
