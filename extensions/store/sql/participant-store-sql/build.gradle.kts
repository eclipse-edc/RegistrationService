plugins {
    `java-library`
}

dependencies {
    api(project(":spi:registration-service-store-spi"))
    api(libs.edc.spi.transaction)
    api(libs.edc.spi.transaction.datasource)
    api(libs.edc.core.sql)

    testImplementation(libs.edc.core.junit)
    testImplementation(testFixtures(libs.edc.core.sql))
    testImplementation(testFixtures(project(":spi:registration-service-store-spi")))
    testImplementation(testFixtures(project(":spi:registration-service-spi")))
}
