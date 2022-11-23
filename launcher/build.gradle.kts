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
    runtimeOnly(project(":extensions:registration-service"))
    runtimeOnly(project(":extensions:participant-verifier"))
    runtimeOnly(project(":extensions:registration-policy-gaiax-member"))
    runtimeOnly(edc.ext.identity.did.web)
    runtimeOnly(edc.ext.identity.did.core)
    runtimeOnly(edc.core.connector)
    runtimeOnly(edc.boot)
    runtimeOnly(edc.core.controlPlane)
    runtimeOnly(edc.ext.observability)
    runtimeOnly(edc.core.micrometer)
    runtimeOnly(edc.ext.micrometer.jetty)
    runtimeOnly(edc.ext.micrometer.jersey)
    runtimeOnly(edc.ext.configuration.filesystem)
    runtimeOnly(identityHub.ext.credentialsVerifier)

    // JDK Logger
    runtimeOnly(edc.ext.jdklogger)

    // To use FileSystem vault e.g. -DuseFsVault="true".Only for non-production usages.
    val useFsVault: Boolean = System.getProperty("useFsVault", "false").toBoolean()
    if (useFsVault) {
        runtimeOnly(edc.ext.vault.filesystem)
    } else {
        runtimeOnly(edc.ext.vault.azure)
    }
}

application {
    mainClass.set("org.eclipse.edc.boot.system.runtime.BaseRuntime")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    mergeServiceFiles()
    archiveFileName.set("app.jar")
}
