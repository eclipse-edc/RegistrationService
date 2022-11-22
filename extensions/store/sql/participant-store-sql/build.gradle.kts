plugins {
    `java-library`
}

dependencies {
    api(project(":spi:participant-store-spi"))
    implementation(edc.spi.transaction)
    implementation(edc.spi.transaction.datasource)
    implementation(edc.core.sql)

    testImplementation(edc.core.junit)
    testImplementation(testFixtures(edc.core.sql))
    testImplementation(testFixtures(project(":spi:participant-store-spi")))
    testImplementation(testFixtures(project(":spi:dataspace-authority-spi")))
    testImplementation(libs.postgres)
}

publishing {
    publications {
        create<MavenPublication>("participant-store-sql") {
            artifactId = "participant-store-sql"
            from(components["java"])
        }
    }
}
