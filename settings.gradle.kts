rootProject.name = "registration-service"

pluginManagement {
    repositories {
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
        create("libs") {
            from("org.eclipse.edc:edc-versions:0.0.1-SNAPSHOT")
            version("picocli", "4.6.3")
            version("googleFindBugs", "3.0.2")
            version("openApiTools", "0.2.1")
            version("swaggerAnnotation", "1.5.22")


            library("picocli-core", "info.picocli", "picocli").versionRef("picocli")
            library("picocli-codegen", "info.picocli", "picocli-codegen").versionRef("picocli")
            library("google-findbugs-jsr305", "com.google.code.findbugs", "jsr305").versionRef("googleFindBugs")
            library(
                "openapi-jackson-databind-nullable",
                "org.openapitools",
                "jackson-databind-nullable"
            ).versionRef("openApiTools")
            library("swagger-annotations", "io.swagger", "swagger-annotations").versionRef("swaggerAnnotation")

        }
        create("identityHub") {
            version("ih", "0.0.1-SNAPSHOT")
            library("spi-core", "org.eclipse.edc", "identity-hub-spi").versionRef("ih")
            library("core", "org.eclipse.edc", "identity-hub").versionRef("ih")
            library("core-api", "org.eclipse.edc", "identity-hub-api").versionRef("ih")
            library("core-client", "org.eclipse.edc", "identity-hub-client").versionRef("ih")
            library("core-verifier", "org.eclipse.edc", "identity-hub-credentials-verifier").versionRef("ih")

            library(
                "ext-verifier-jwt", "org.eclipse.edc", "identity-hub-verifier-jwt"
            ).versionRef("ih")
            library(
                "ext-credentials-jwt", "org.eclipse.edc", "identity-hub-credentials-jwt"
            ).versionRef("ih")

        }
        create("edc") {
            version("edc", "0.0.1-SNAPSHOT")
            library("util", "org.eclipse.edc", "util").versionRef("edc")
            library("boot", "org.eclipse.edc", "boot").versionRef("edc")

            library("spi-core", "org.eclipse.edc", "core-spi").versionRef("edc")
            library("spi-policy-engine", "org.eclipse.edc", "policy-engine-spi").versionRef("edc")
            library("spi-transaction", "org.eclipse.edc", "transaction-spi").versionRef("edc")
            library("spi-transaction-datasource", "org.eclipse.edc", "transaction-datasource-spi").versionRef("edc")
            library("spi-identity-did", "org.eclipse.edc", "identity-did-spi").versionRef("edc")

            library("core-connector", "org.eclipse.edc", "connector-core").versionRef("edc")
            library("core-controlPlane", "org.eclipse.edc", "control-plane-core").versionRef("edc")
            library("core-micrometer", "org.eclipse.edc", "micrometer-core").versionRef("edc")
            library("core-api", "org.eclipse.edc", "api-core").versionRef("edc")
            library("core-stateMachine", "org.eclipse.edc", "state-machine").versionRef("edc")
            library("core-sql", "org.eclipse.edc", "sql-core").versionRef("edc")
            library("core-junit", "org.eclipse.edc", "junit").versionRef("edc")

            library("ext-identity-did-crypto", "org.eclipse.edc", "identity-did-crypto").versionRef("edc")
            library("ext-identity-did-core", "org.eclipse.edc", "identity-did-core").versionRef("edc")
            library("ext-identity-did-web", "org.eclipse.edc", "identity-did-web").versionRef("edc")
            library("ext-http", "org.eclipse.edc", "http").versionRef("edc")
            library("ext-micrometer-jetty", "org.eclipse.edc", "jetty-micrometer").versionRef("edc")
            library("ext-micrometer-jersey", "org.eclipse.edc", "jersey-micrometer").versionRef("edc")
            library("ext-observability", "org.eclipse.edc", "api-observability").versionRef("edc")
            library("ext-configuration-filesystem", "org.eclipse.edc", "configuration-filesystem").versionRef("edc")
            library("ext-vault-filesystem", "org.eclipse.edc", "vault-filesystem").versionRef("edc")
            library("ext-vault-azure", "org.eclipse.edc", "vault-azure").versionRef("edc")
            library("ext-azure-cosmos-core", "org.eclipse.edc", "azure-cosmos-core").versionRef("edc")
            library("ext-azure-test", "org.eclipse.edc", "azure-test").versionRef("edc")
            library("ext-jdklogger", "org.eclipse.edc", "monitor-jdk-logger").versionRef("edc")

        }
    }
}

include(":spi:participant-store-spi")
include(":spi:dataspace-authority-spi")
include(":launcher")
include(":extensions:registration-service")
include(":extensions:participant-verifier")
include(":extensions:registration-policy-gaiax-member")
include(":system-tests")
include(":system-tests:launchers:participant")
include(":rest-client")
include(":client-cli")
include(":extensions:store:sql:participant-store-sql")
include(":extensions:store:cosmos:participant-store-cosmos")
