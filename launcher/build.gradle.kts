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
    id("application")
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

dependencies {
    implementation(project(":extensions:registration-service"))
    implementation(project(":extensions:participant-verifier"))
    implementation(project(":extensions:registration-policy-gaiax-member"))
    implementation(edc.ext.identity.did.web)
    implementation(edc.ext.identity.did.core)
    implementation(edc.core.connector)
    runtimeOnly(edc.boot)
    implementation(edc.core.controlPlane)
    implementation(edc.ext.observability)
    implementation(edc.core.micrometer)
    runtimeOnly(edc.ext.micrometer.jetty)
    runtimeOnly(edc.ext.micrometer.jersey)
    implementation(edc.ext.configuration.filesystem)
    implementation(identityHub.ext.credentialsVerifier)

    // JDK Logger
    implementation(edc.ext.jdklogger)

    // To use FileSystem vault e.g. -DuseFsVault="true".Only for non-production usages.
    val useFsVault: Boolean = System.getProperty("useFsVault", "false").toBoolean()
    if (useFsVault) {
        implementation(edc.ext.vault.filesystem)
    } else {
        implementation(edc.ext.vault.azure)
    }
}

application {
    mainClass.set("org.eclipse.edc.boot.system.runtime.BaseRuntime")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    mergeServiceFiles()
    archiveFileName.set("app.jar")
}
