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
    `java-library`
    id("org.openapi.generator") version "5.4.0"
    `maven-publish`
    `java-test-fixtures`
}

// Configure OpenAPI Generator
tasks.withType(org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class.java) {
    generatorName.value("java")
    inputSpec.value(file("$rootDir/resources/openapi/yaml/registration-service.yaml").absolutePath)
    validateSpec.value(false)
    configOptions.set(
        mapOf(
            "library" to "native",
            "dateLibrary" to "legacy",
            "useRuntimeException" to "true",
            "invokerPackage" to "org.eclipse.edc.registration.client",
            "apiPackage" to "org.eclipse.edc.registration.client.api",
            "modelPackage" to "org.eclipse.edc.registration.client.models",
        )
    )
}

// Ensure compileJava depends on openApiGenerate
val compileJava: JavaCompile by tasks
val openApiGenerate: org.openapitools.generator.gradle.plugin.tasks.GenerateTask by tasks
compileJava.apply {
    dependsOn(openApiGenerate)
}

// Add generated sources
sourceSets {
    main {
        java {
            srcDirs(
                "$buildDir/generate-resources/main/src/main/java"
            )
        }
    }
}

val edcVersion: String by project
val edcGroup: String by project
val jacksonVersion: String by project
val jupiterVersion: String by project
val assertj: String by project


dependencies {
    api("${edcGroup}:identity-did-crypto:${edcVersion}")
    api("${edcGroup}:util:${edcVersion}")

    testImplementation("org.assertj:assertj-core:${assertj}")
    testImplementation("org.junit.jupiter:junit-jupiter-api:${jupiterVersion}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${jupiterVersion}")

    // Dependencies copied from build/generate-resources/main/build.gradle
    implementation("io.swagger:swagger-annotations:1.5.22")
    implementation("com.google.code.findbugs:jsr305:3.0.2")
    implementation("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    implementation("org.openapitools:jackson-databind-nullable:0.2.1")
    implementation("jakarta.annotation:jakarta.annotation-api:1.3.5")
}

publishing {
    publications {
        create<MavenPublication>("registration-service-client") {
            artifactId = "registration-service-client"
            from(components["java"])
        }
    }
}
