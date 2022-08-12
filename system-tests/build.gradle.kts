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

val edcVersion: String by project
val edcGroup: String by project
val identityHubVersion: String by project
val identityHubGroup: String by project
val awaitility: String by project
val jupiterVersion: String by project
val okHttpVersion: String by project
val assertj: String by project
val jacksonVersion: String by project
val faker: String by project

dependencies {
    testImplementation(testFixtures(project(":rest-client")))
    testImplementation(project(":rest-client"))
    testImplementation(project(":client-cli"))
    testImplementation("com.squareup.okhttp3:okhttp:${okHttpVersion}")
    testImplementation("${identityHubGroup}:identity-hub-client:${identityHubVersion}")
    testRuntimeOnly(project(":launcher"))
    testImplementation("org.assertj:assertj-core:${assertj}")
    testImplementation("org.junit.jupiter:junit-jupiter-api:${jupiterVersion}")
    testImplementation("com.fasterxml.jackson.core:jackson-databind:${jacksonVersion}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${jupiterVersion}")
    testImplementation("org.awaitility:awaitility:${awaitility}")
    testImplementation("com.github.javafaker:javafaker:${faker}")
    testImplementation("${edcGroup}:junit:${edcVersion}")
}
