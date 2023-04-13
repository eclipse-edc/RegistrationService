rootProject.name = "registration-service"

pluginManagement {
    repositories {
        mavenLocal()
        maven {
            url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {

    repositories {
        maven {
            url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
        }
        mavenCentral()
        mavenLocal()
    }
}

include(":core:registration-service")
include(":core:registration-service-client")
include(":core:registration-service-credential-service")
include(":core:registration-service-onboarding-policy-verifier")
include(":extensions:registration-service-api")
include(":extensions:store:cosmos:participant-store-cosmos")
include(":extensions:store:sql:participant-store-sql")
include(":launcher")
include(":registration-service-cli")
include(":spi:registration-service-spi")
include(":spi:registration-service-store-spi")
include(":system-tests")
include(":system-tests:launchers:participant")
include(":version-catalog")
