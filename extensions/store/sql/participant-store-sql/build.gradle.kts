plugins {
    `java-library`
}

dependencies {
    api(project(":spi:participant-store-spi"))
    api(edc.spi.transaction)
    api(edc.spi.transaction.datasource)
    api(edc.core.sql)

    testImplementation(edc.core.junit)
    testImplementation(testFixtures(edc.core.sql))
    testImplementation(testFixtures(project(":spi:participant-store-spi")))
    testImplementation(testFixtures(project(":spi:dataspace-authority-spi")))
}

publishing {
    publications {
        create<MavenPublication>("participant-store-sql") {
            artifactId = "participant-store-sql"
            from(components["java"])
        }
    }
}
