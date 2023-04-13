plugins {
    `java-library`
}

dependencies {
    api(project(":spi:registration-service-store-spi"))
    api(libs.edc.spi.transaction)

    implementation(libs.edc.core.stateMachine)
    implementation(libs.edc.ext.http)
    implementation(libs.edc.ext.identity.did.crypto)
    implementation(libs.ih.ext.verifier.jwt)
    implementation(libs.ih.ext.credentials.jwt)

    implementation(libs.opentelemetry.annotations)

    testImplementation(testFixtures(project(":spi:registration-service-spi")))
    testImplementation(testFixtures(project(":spi:registration-service-store-spi")))
    testImplementation(testFixtures(project(":core:registration-service-client")))
}

publishing {
    publications {
        create<MavenPublication>(project.name) {
            from(components["java"])
        }
    }
}

