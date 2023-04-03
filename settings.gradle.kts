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
    versionCatalogs {
        create("root") {
            from("org.eclipse.edc:edc-versions:0.0.1-SNAPSHOT")
        }
    }
}

include(":core:registration-service")
include(":core:registration-service-credential-service")
include(":core:registration-service-onboarding-policy-verifier")
include(":core:registration-service-client")

include(":extensions:registration-service-api")
include(":extensions:store:cosmos:participant-store-cosmos")
include(":extensions:store:sql:participant-store-sql")

include(":spi:registration-service-spi")
include(":spi:registration-service-store-spi")

include(":system-tests")
include(":system-tests:launchers:participant")

include(":registration-service-cli")

include(":launcher")


