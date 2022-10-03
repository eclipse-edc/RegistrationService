plugins {
    `java-library`
    id("io.swagger.core.v3.swagger-gradle-plugin")
}

val edcVersion: String by project
val edcGroup: String by project
val identityHubVersion: String by project
val identityHubGroup: String by project
val okHttpVersion: String by project
val jupiterVersion: String by project
val assertj: String by project
val mockitoVersion: String by project
val openTelemetryVersion: String by project

dependencies {
    implementation("${edcGroup}:http:${edcVersion}")
    implementation("${edcGroup}:state-machine:${edcVersion}")
    implementation("${edcGroup}:identity-did-crypto:${edcVersion}")
    implementation("${identityHubGroup}:identity-hub-client:${identityHubVersion}")
    implementation("${edcGroup}:api-core:${edcVersion}")
    implementation("io.opentelemetry:opentelemetry-extension-annotations:${openTelemetryVersion}")

    implementation(project(":extensions:participant-store-spi"))
    implementation("com.squareup.okhttp3:okhttp:${okHttpVersion}")

    testImplementation("org.assertj:assertj-core:${assertj}")
    testImplementation("org.junit.jupiter:junit-jupiter-api:${jupiterVersion}")
    testImplementation("org.junit.jupiter:junit-jupiter-params:${jupiterVersion}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${jupiterVersion}")
    testImplementation("org.mockito:mockito-core:${mockitoVersion}")
    testImplementation(testFixtures(project(":extensions:dataspace-authority-spi")))
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

