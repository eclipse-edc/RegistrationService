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

// REST client using OpenAPI Generator. See:
// https://github.com/OpenAPITools/openapi-generator/tree/master/modules/openapi-generator-gradle-plugin
// https://github.com/OpenAPITools/openapi-generator/blob/master/docs/generators/java.md

plugins {
    id("java")
    `java-library`
    `maven-publish`
    `java-test-fixtures`
}

dependencies {
    implementation(edc.ext.identity.did.crypto)
    implementation(edc.util)
    implementation(edc.spi.http)
    implementation(edc.core.connector)
    implementation(libs.jackson.core)
    implementation(libs.bundles.jackson)
    implementation(libs.openapi.jackson.databind.nullable)

    testImplementation("com.squareup.okhttp3:mockwebserver:4.10.0")
}
