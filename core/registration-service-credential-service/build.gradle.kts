plugins {
    `java-library`
}

dependencies {
    api(project(":spi:registration-service-store-spi"))

    implementation(edc.ext.http)
    implementation(edc.ext.identity.did.crypto)
    implementation(identityHub.core.client)
    implementation(identityHub.ext.verifier.jwt)
    implementation(identityHub.ext.credentials.jwt)

    implementation(libs.okhttp)

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

