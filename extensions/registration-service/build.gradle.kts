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
val faker: String by project

dependencies {
    implementation("${edcGroup}:http:${edcVersion}")
    implementation("${edcGroup}:state-machine-lib:${edcVersion}")
    implementation("${edcGroup}:identity-did-crypto:${edcVersion}")
    implementation("${identityHubGroup}:identity-hub-spi:${identityHubVersion}")
    implementation("${identityHubGroup}:identity-hub-client:${identityHubVersion}")

    implementation(project(":extensions:participant-store-spi"))
    implementation(project(":extensions:dataspace-authority-spi"))
    implementation("com.squareup.okhttp3:okhttp:${okHttpVersion}")

    testImplementation("org.assertj:assertj-core:${assertj}")
    testImplementation("org.junit.jupiter:junit-jupiter-api:${jupiterVersion}")
    testImplementation("org.junit.jupiter:junit-jupiter-params:${jupiterVersion}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${jupiterVersion}")
    testImplementation("org.mockito:mockito-core:${mockitoVersion}")
    testImplementation("com.github.javafaker:javafaker:${faker}")
    testImplementation(testFixtures(project(":extensions:dataspace-authority-spi")))
    testImplementation(testFixtures(project(":rest-client")))
}

