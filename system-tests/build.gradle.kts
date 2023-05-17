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
    testImplementation(libs.ih.core.client)
    testImplementation(libs.ih.ext.verifier.jwt)
    testImplementation(libs.ih.ext.credentials.jwt)
    testImplementation(libs.edc.spi.identity.did)
    testRuntimeOnly(project(":launcher"))
    testImplementation(libs.awaitility)
    testImplementation(libs.mockserver.netty)
    testImplementation(libs.mockserver.client)
    testImplementation(libs.edc.core.junit)
}

edcBuild {
    publish.set(false)
}
