plugins {
    `java-library`
    `java-test-fixtures`
}

dependencies {
    api(project(":spi:dataspace-authority-spi"))

    implementation(libs.jackson.databind)

    testFixturesImplementation(libs.junit.jupiter.api)
    testFixturesImplementation(libs.junit.jupiter.params)
    testFixturesImplementation(libs.assertj)

}

publishing {
    publications {
        create<MavenPublication>("participant-store-spi") {
            artifactId = "participant-store-spi"
            from(components["java"])
        }
    }
}
