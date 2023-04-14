plugins {
    `java-library`
    id("io.swagger.core.v3.swagger-gradle-plugin")
}

dependencies {
    api(project(":spi:registration-service-spi"))

    implementation(libs.edc.core.api)
    implementation(libs.edc.ext.http)
    implementation(libs.edc.ext.identity.did.crypto)

    testImplementation(testFixtures(project(":spi:registration-service-spi")))
    testImplementation(testFixtures(project(":core:registration-service-client")))
}

edcBuild {
    swagger {
        apiGroup.set("management-api")
    }
}
