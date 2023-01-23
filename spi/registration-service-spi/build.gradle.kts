plugins {
    `java-library`
    `java-test-fixtures`
}

dependencies {
    api(edc.spi.core)
    api(edc.spi.policy.engine)
    api(edc.spi.aggregate.service)
    api(identityHub.spi.core)
    implementation(libs.jackson.databind)
}
