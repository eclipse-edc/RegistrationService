plugins {
    `java-library`
    id("application")
    alias(libs.plugins.shadow)
}

dependencies {
    api(libs.picocli.core)
    annotationProcessor(libs.picocli.codegen)

    api(project(":core:registration-service-client"))
    implementation(root.jackson.databind)

    implementation(libs.edc.ext.identity.did.web)
    implementation(libs.edc.ext.identity.did.crypto)
    implementation(root.okhttp)
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
