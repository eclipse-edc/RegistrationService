plugins {
    `java-library`
}

dependencies {
    api(project(":spi:registration-service-store-spi"))
    api(edc.spi.transaction)
    api(edc.spi.transaction.datasource)
    api(edc.core.sql)

    testImplementation(edc.core.junit)
    testImplementation(testFixtures(edc.core.sql))
    testImplementation(testFixtures(project(":spi:registration-service-store-spi")))
    testImplementation(testFixtures(project(":spi:registration-service-spi")))
}
