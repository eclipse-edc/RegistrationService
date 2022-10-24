/*
 *  Copyright (c) 2020, 2022 Microsoft Corporation
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
val assertj: String by project
val jupiterVersion: String by project
val mockitoVersion: String by project
val cosmosSdkVersion: String by project
val failsafeVersion: String by project

dependencies {
    api(project(":spi:participant-store-spi"))
    api("${edcGroup}:azure-cosmos-core:${edcVersion}")

    implementation("dev.failsafe:failsafe:${failsafeVersion}")
    implementation("com.azure:azure-cosmos:${cosmosSdkVersion}")


    testImplementation(testFixtures(project(":spi:participant-store-spi")))
    testImplementation("org.assertj:assertj-core:${assertj}")
    testImplementation("org.mockito:mockito-core:${mockitoVersion}")
    testImplementation("org.junit.jupiter:junit-jupiter-api:${jupiterVersion}")
    testImplementation("org.junit.jupiter:junit-jupiter-params:${jupiterVersion}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${jupiterVersion}")
    testImplementation(testFixtures("${edcGroup}:azure-test:${edcVersion}"))

}

