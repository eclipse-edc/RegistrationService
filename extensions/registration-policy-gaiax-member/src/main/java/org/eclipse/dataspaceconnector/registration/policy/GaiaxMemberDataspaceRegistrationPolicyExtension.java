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

import org.eclipse.dataspaceconnector.policy.model.AtomicConstraint;
import org.eclipse.dataspaceconnector.policy.model.LiteralExpression;
import org.eclipse.dataspaceconnector.policy.model.Operator;
import org.eclipse.dataspaceconnector.policy.model.Permission;
import org.eclipse.dataspaceconnector.policy.model.Policy;
import org.eclipse.dataspaceconnector.registration.DataspaceRegistrationPolicy;
import org.eclipse.dataspaceconnector.spi.policy.PolicyContext;
import org.eclipse.dataspaceconnector.spi.policy.PolicyEngine;
import org.eclipse.dataspaceconnector.spi.policy.RuleBindingRegistry;
import org.eclipse.dataspaceconnector.spi.system.Inject;
import org.eclipse.dataspaceconnector.spi.system.Provider;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension;

import java.util.Objects;

import static org.eclipse.dataspaceconnector.registration.DataspaceRegistrationPolicy.PARTICIPANT_REGISTRATION_SCOPE;

/**
 * EDC extension to create a policy that accepts participants with a GAIA-X membership credential.
 */
public class GaiaxMemberDataspaceRegistrationPolicyExtension implements ServiceExtension {
    private static final String CREDENTIAL_NAME = "gaiaXMember";
    public static final String RULE_TYPE = CREDENTIAL_NAME;
    public static final String CREDENTIAL_VALUE = "true";

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
                .operator(Operator.EQ)
                .rightExpression(new LiteralExpression(CREDENTIAL_VALUE)).build();
        var permission = Permission.Builder.newInstance().constraint(constraint).build();
        var policy = Policy.Builder.newInstance()
                .permission(permission).build();
        ruleBindingRegistry.bind(RULE_TYPE, PARTICIPANT_REGISTRATION_SCOPE);
        policyEngine.registerFunction(PARTICIPANT_REGISTRATION_SCOPE, Permission.class, RULE_TYPE, GaiaxMemberDataspaceRegistrationPolicyExtension::evaluate);
        return new DataspaceRegistrationPolicy(policy);
    }

    private static boolean evaluate(Operator operator, Object rightOperand, Permission rule, PolicyContext context) {
        var value = context.getParticipantAgent().getClaims().get(CREDENTIAL_NAME);
        switch (operator) {
            case EQ:
                return Objects.equals(value, rightOperand);
            default:
                return false;
        }
    }
}
