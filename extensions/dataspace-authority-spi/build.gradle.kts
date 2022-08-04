plugins {
    `java-library`
    `java-test-fixtures`
}

val jacksonVersion: String by project
val jupiterVersion: String by project
val assertj: String by project
val mockitoVersion: String by project
val faker: String by project
val swagger: String by project

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:${jacksonVersion}")
    implementation("io.swagger.core.v3:swagger-annotations:${swagger}")
    testImplementation("org.assertj:assertj-core:${assertj}")
    testImplementation("org.junit.jupiter:junit-jupiter-api:${jupiterVersion}")
    testImplementation("org.junit.jupiter:junit-jupiter-params:${jupiterVersion}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${jupiterVersion}")
    testFixturesImplementation("com.github.javafaker:javafaker:${faker}")
}

