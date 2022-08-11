plugins {
    `java-library`
}

val jacksonVersion: String by project
val jetBrainsAnnotationsVersion: String by project

dependencies {
    api(project(":extensions:dataspace-authority-spi"))
    api("org.jetbrains:annotations:${jetBrainsAnnotationsVersion}")

    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
}

