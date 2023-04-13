plugins {
    `java-library`
    `java-test-fixtures`
}

dependencies {
    api(libs.edc.spi.core)
    api(libs.edc.spi.policy.engine)
    api(libs.edc.spi.aggregate.service)
    api(libs.ih.spi.core)
    implementation(libs.jackson.databind)
}
