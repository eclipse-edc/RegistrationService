plugins {
    `java-library`
    id("application")
    alias(libs.plugins.shadow)
}

dependencies {
    api(project(":core:registration-service-client"))
    api(libs.picocli.core)
    annotationProcessor(libs.picocli.codegen)

    implementation(libs.edc.ext.identity.did.web)
    implementation(libs.edc.ext.identity.did.crypto)
    implementation(libs.edc.core.connector)

    testImplementation(testFixtures(project(":core:registration-service-client")))
}

application {
    mainClass.set("org.eclipse.edc.registration.cli.RegistrationServiceCli")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    mergeServiceFiles()
    archiveFileName.set("registration-service-cli.jar")
}
