plugins {
    `java-library`
}

dependencies {
    api(libs.edc.spi.core)
    api(libs.edc.spi.identity.did)
    api(project(":spi:registration-service-spi"))
}
