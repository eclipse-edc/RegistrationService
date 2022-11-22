plugins {
    `java-library`
    `java-test-fixtures`
}

dependencies {
    api(edc.spi.core)
    api(edc.spi.policy.engine)
    implementation(libs.jackson.databind)
}

publishing {
    publications {
        create<MavenPublication>("dataspace-authority-spi") {
            artifactId = "dataspace-authority-spi"
            from(components["java"])
        }
    }
}
