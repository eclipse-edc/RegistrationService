plugins {
    `java-library`
}

dependencies {
    api(project(":spi:registration-service-store-spi"))

    implementation(libs.edc.ext.http)
    implementation(libs.edc.ext.identity.did.crypto)
    implementation(libs.ih.core.client)
    implementation(libs.ih.ext.verifier.jwt)
    implementation(libs.ih.ext.credentials.jwt)

    testImplementation(testFixtures(project(":spi:registration-service-spi")))
    testImplementation(testFixtures(project(":core:registration-service-client")))
}

publishing {
    publications {
        create<MavenPublication>(project.name) {
            from(components["java"])
        }
    }
}

