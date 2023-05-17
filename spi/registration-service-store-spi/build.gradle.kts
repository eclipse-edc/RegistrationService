plugins {
    `java-library`
    `java-test-fixtures`
}

dependencies {
    api(project(":spi:registration-service-spi"))

    testFixturesImplementation(libs.junit.jupiter.api)
    testFixturesImplementation(libs.junit.jupiter.params)
    testFixturesImplementation(libs.assertj)

}
