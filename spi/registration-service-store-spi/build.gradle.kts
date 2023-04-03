plugins {
    `java-library`
    `java-test-fixtures`
}

dependencies {
    api(project(":spi:registration-service-spi"))

    implementation(root.jackson.databind)

    testFixturesImplementation(root.junit.jupiter.api)
    testFixturesImplementation(root.junit.jupiter.params)
    testFixturesImplementation(root.assertj)

}
