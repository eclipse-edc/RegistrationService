plugins {
    `java-library`
    id("io.swagger.core.v3.swagger-gradle-plugin")
}

dependencies {
    api(project(":spi:registration-service-spi"))

    implementation(edc.core.api)
    implementation(edc.ext.http)
    implementation(edc.ext.identity.did.crypto)

    implementation(libs.opentelemetry.annotations)

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

