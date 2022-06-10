plugins {
    `java-library`
    id("application")
    id("com.github.johnrengelman.shadow") version "7.0.0"
    `maven-publish`
}

val edcVersion: String by project
val edcGroup: String by project
val jacksonVersion: String by project

dependencies {
    api("info.picocli:picocli:4.6.3")
    annotationProcessor("info.picocli:picocli-codegen:4.6.3")

    api(project(":rest-client"))
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
}

application {
    mainClass.set("org.eclipse.dataspaceconnector.registration.cli.RegistrationServiceCli")
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
