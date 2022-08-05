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

package org.eclipse.dataspaceconnector.registration;

import org.eclipse.dataspaceconnector.iam.did.spi.credentials.CredentialsVerifier;
import org.eclipse.dataspaceconnector.iam.did.spi.resolution.DidResolverRegistry;
import org.eclipse.dataspaceconnector.policy.model.AtomicConstraint;
import org.eclipse.dataspaceconnector.policy.model.LiteralExpression;
import org.eclipse.dataspaceconnector.policy.model.Operator;
import org.eclipse.dataspaceconnector.policy.model.Permission;
import org.eclipse.dataspaceconnector.policy.model.Policy;
import org.eclipse.dataspaceconnector.spi.policy.PolicyEngine;
import org.eclipse.dataspaceconnector.spi.policy.RuleBindingRegistry;
import org.eclipse.dataspaceconnector.spi.result.Result;
import org.eclipse.dataspaceconnector.spi.system.Inject;
import org.eclipse.dataspaceconnector.spi.system.Provider;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext;

import java.util.Map;
import java.util.UUID;

import static org.eclipse.dataspaceconnector.registration.DataspacePolicy.ONBOARDING_SCOPE;

/**
 * EDC extension to boot the services used by the Authority Service.
 */
public class MocksExtension implements ServiceExtension {
    private static final String VERIFIABLE_CREDENTIAL_ID_KEY = "id";
    private static final String CREDENTIAL_SUBJECT_KEY = "credentialSubject";
    private static final String ISSUER_KEY = "iss";
    private static final String VERIFIABLE_CREDENTIALS_KEY = "vc";
    private static final String REGION = "region";
    public static final String RULE_TYPE_REGION = REGION;

    @Inject
    private RuleBindingRegistry ruleBindingRegistry;

    @Inject
    private PolicyEngine policyEngine;

    @Inject
    private DidResolverRegistry didResolverRegistry;

    @Override
    public void initialize(ServiceExtensionContext context) {
        // catch all "did:mock:..." did urls
        didResolverRegistry.register(new MockDidResolver());
    }

    @Override
    public String name() {
        return "---->> Registration Service Mocks Extension :: REMOVE FOR PRODUCTION!!! <<----";
    }

    @Provider
    public CredentialsVerifier createRegionIsEuVerifier() {
        //every participant always has these VCs:
        var vcId = UUID.randomUUID().toString();
        return (participant) -> Result.success(Map.of(vcId,
                Map.of(VERIFIABLE_CREDENTIALS_KEY,
                        Map.of(CREDENTIAL_SUBJECT_KEY, Map.of(REGION, "eu"),
                                VERIFIABLE_CREDENTIAL_ID_KEY, vcId),
                        // issuer will be ignored when applying policies for now.
                        ISSUER_KEY, String.join("did:web:", UUID.randomUUID().toString()))));
    }

    /**
     * Performs the plumbing for registering a {@link Policy} and an evaluation function that checks that "region EQ
     * eu". The rule type {@link MocksExtension#RULE_TYPE_REGION} is bound to the onboarding scope
     * {@link DataspacePolicy#ONBOARDING_SCOPE}.
     */
    @Provider
    public DataspacePolicy createDataspacePolicy() {

        var regionConstraint = AtomicConstraint.Builder.newInstance().leftExpression(new LiteralExpression(RULE_TYPE_REGION))
                .operator(Operator.EQ)
                .rightExpression(new LiteralExpression("eu")).build();
        var regionPermission = Permission.Builder.newInstance().constraint(regionConstraint).build();
        var p = Policy.Builder.newInstance()
                .permission(regionPermission).build();
        ruleBindingRegistry.bind(RULE_TYPE_REGION, ONBOARDING_SCOPE);
        policyEngine.registerFunction(ONBOARDING_SCOPE, Permission.class, RULE_TYPE_REGION, (operator, rightValue, rule, context) -> Operator.EQ == operator && "eu".equalsIgnoreCase(rightValue.toString()));
        return new DataspacePolicy(p);
    }
}
