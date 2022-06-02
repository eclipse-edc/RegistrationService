plugins {
    `java-library`
    id("io.swagger.core.v3.swagger-gradle-plugin")
}

val rsApi : String by project
val edcVersion: String by project
val edcGroup: String by project

dependencies{
    implementation("${edcGroup}:core:${edcVersion}")
}

