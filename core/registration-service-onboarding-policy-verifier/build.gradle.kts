plugins {
    `java-library`
}

dependencies {
    api(edc.spi.core)
    api(edc.spi.identity.did)
    api(project(":spi:registration-service-spi"))
}

publishing {
    publications {
        create<MavenPublication>(project.name) {
            from(components["java"])
        }
    }
}
