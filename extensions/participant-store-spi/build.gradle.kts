plugins {
    `java-library`
    `java-test-fixtures`
}

val jacksonVersion: String by project
val jetBrainsAnnotationsVersion: String by project
val jupiterVersion: String by project
val assertj: String by project

dependencies {
    api(project(":extensions:dataspace-authority-spi"))
    api("org.jetbrains:annotations:${jetBrainsAnnotationsVersion}")

    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")

    testFixturesImplementation("org.junit.jupiter:junit-jupiter-api:${jupiterVersion}")
    testFixturesImplementation("org.junit.jupiter:junit-jupiter-params:${jupiterVersion}")
    testFixturesRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${jupiterVersion}")
    testFixturesImplementation("org.assertj:assertj-core:${assertj}")

}

publishing {
    publications {
        create<MavenPublication>("participant-store-spi") {
            artifactId = "participant-store-spi"
            from(components["java"])
        }
    }
}
