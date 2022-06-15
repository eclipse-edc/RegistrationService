plugins {
    `java-library`
    `java-test-fixtures`
}

val jacksonVersion: String by project
val faker: String by project

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    testFixturesImplementation("com.github.javafaker:javafaker:${faker}")
}

