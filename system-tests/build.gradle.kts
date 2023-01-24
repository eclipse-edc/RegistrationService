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

plugins {
    `java-library`
}

dependencies {
    testImplementation(testFixtures(project(":core:registration-service-client")))
    testImplementation(project(":core:registration-service-client"))
    testImplementation(project(":registration-service-cli"))
    testImplementation(libs.okhttp)
    testImplementation(identityHub.core.client)
    testImplementation(identityHub.ext.verifier.jwt)
    testImplementation(identityHub.ext.credentials.jwt)
    testImplementation(edc.spi.identity.did)
    testRuntimeOnly(project(":launcher"))
    testImplementation(libs.awaitility)
    testImplementation(libs.jackson.databind)
    testImplementation(libs.mockserver.netty)
    testImplementation(libs.mockserver.client)
    testImplementation(edc.core.junit)
}
