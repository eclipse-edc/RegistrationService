plugins {
    `java-library`
    id("application")
    id("com.github.johnrengelman.shadow") version "7.0.0"
    `maven-publish`
}

dependencies {
    api(libs.picocli.core)
    annotationProcessor(libs.picocli.codegen)

    api(project(":registration-service-client"))
    implementation(libs.jackson.databind)

    implementation(edc.ext.identity.did.web)
    implementation(edc.ext.identity.did.crypto)
    implementation(libs.okhttp)
    implementation(edc.core.connector)

    testImplementation(testFixtures(project(":registration-service-client")))
}

application {
    mainClass.set("org.eclipse.edc.registration.cli.RegistrationServiceCli")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    mergeServiceFiles()
    archiveFileName.set("registration-service-cli.jar")
}

publishing {
    publications {
        create<MavenPublication>("registration-service-cli") {
            artifactId = "registration-service-cli"
            from(components["java"])
        }
    }
}
