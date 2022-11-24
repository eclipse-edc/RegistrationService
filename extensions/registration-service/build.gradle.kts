plugins {
    `java-library`
    id("io.swagger.core.v3.swagger-gradle-plugin")
}

dependencies {
    api(project(":spi:participant-store-spi"))

    implementation(edc.core.api)
    implementation(edc.core.stateMachine)
    implementation(edc.ext.http)
    implementation(edc.ext.identity.did.crypto)
    implementation(identityHub.core.client)

    implementation(libs.opentelemetry.annotations)
    implementation(libs.okhttp)

    testImplementation(testFixtures(project(":spi:dataspace-authority-spi")))
    testImplementation(testFixtures(project(":spi:participant-store-spi")))
    testImplementation(testFixtures(project(":rest-client")))
}

publishing {
    publications {
        create<MavenPublication>("registration-service") {
            artifactId = "registration-service"
            from(components["java"])
        }
    }
}

