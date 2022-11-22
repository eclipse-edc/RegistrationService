plugins {
    `java-library`
    id("io.swagger.core.v3.swagger-gradle-plugin")
}

dependencies {
    api(project(":spi:dataspace-authority-spi"))

    testImplementation(edc.core.junit)
}

publishing {
    publications {
        create<MavenPublication>("registration-policy-gaiax-member") {
            artifactId = "registration-policy-gaiax-member"
            from(components["java"])
        }
    }
}
