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
    id("java")
    `java-library`
    `java-test-fixtures`
}

dependencies {
    implementation(libs.edc.ext.identity.did.crypto)
    implementation(libs.edc.util)
    implementation(libs.edc.spi.http)
    implementation(libs.edc.core.connector)
    implementation(libs.jackson.databind)
    implementation(libs.openapi.jackson.databind.nullable)

    testImplementation(libs.okhttp.mockwebserver)

    testFixturesImplementation(libs.jetbrains.annotations)
}
