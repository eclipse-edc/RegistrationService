plugins {
    `java-library`
}

dependencies {
    api(edc.spi.core)
    api(edc.spi.identity.did)
    api(project(":spi:dataspace-authority-spi"))

}

publishing {
    publications {
        create<MavenPublication>("participant-verifier") {
            artifactId = "participant-verifier"
            from(components["java"])
        }
    }
}
