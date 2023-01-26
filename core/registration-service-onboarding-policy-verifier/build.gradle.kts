plugins {
    `java-library`
}

dependencies {
    api(edc.spi.core)
    api(edc.spi.identity.did)
    api(project(":spi:registration-service-spi"))
}
