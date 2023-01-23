plugins {
    `java-library`
    `java-test-fixtures`
}

dependencies {
    api(project(":spi:registration-service-spi"))

    implementation(libs.jackson.databind)

    testFixturesImplementation(libs.junit.jupiter.api)
    testFixturesImplementation(libs.junit.jupiter.params)
    testFixturesImplementation(libs.assertj)

}

publishing {
    publications {
        create<MavenPublication>(project.name) {
            from(components["java"])
        }
    }
}
