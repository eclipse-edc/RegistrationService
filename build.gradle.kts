plugins {
    java
    `java-library`
    signing
    `maven-publish`
    id("org.gradle.crypto.checksum") version "1.4.0"
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
    jacoco
}

val projectGroup: String by project
val swagger: String by project
val rsApi: String by project

// these values are required for the project POM (for publishing)
val edcDeveloperId: String by project
val edcDeveloperName: String by project
val edcDeveloperEmail: String by project
val edcScmConnection: String by project
val edcWebsiteUrl: String by project
val edcScmUrl: String by project

val defaultVersion: String by project
// makes the project version overridable using the "-PregSrvVersion=..." flag. Useful for CI builds
val projectVersion: String = (project.findProperty("regSrvVersion") ?: defaultVersion) as String

var deployUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"

if (projectVersion.contains("SNAPSHOT")) {
    deployUrl = "https://oss.sonatype.org/content/repositories/snapshots/"
}

subprojects{
    afterEvaluate {
        publishing {
            publications.forEach { i ->
                val mp = (i as MavenPublication)
                mp.pom {
                    name.set(project.name)
                    description.set("edc :: ${project.name}")
                    url.set(edcWebsiteUrl)

                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                        developers {
                            developer {
                                id.set(edcDeveloperId)
                                name.set(edcDeveloperName)
                                email.set(edcDeveloperEmail)
                            }
                        }
                        scm {
                            connection.set(edcScmConnection)
                            url.set(edcScmUrl)
                        }
                    }
                }
            }
        }
    }
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")
    version = projectVersion
    group = projectGroup

    repositories {
        mavenCentral()
        mavenLocal()
        maven {
            url = uri("https://maven.iais.fraunhofer.de/artifactory/eis-ids-public/")
        }
        maven{
            url= uri("https://oss.sonatype.org/content/repositories/snapshots/")
        }
    }

    if (!project.hasProperty("skip.signing")) {
        apply(plugin = "signing")
        publishing {
            repositories {
                maven {
                    name = "OSSRH"
                    setUrl(deployUrl)
                    credentials {
                        username = System.getenv("OSSRH_USER") ?: return@credentials
                        password = System.getenv("OSSRH_PASSWORD") ?: return@credentials
                    }
                }
            }

            signing {
                useGpgCmd()
                sign(publishing.publications)
            }
        }
    }

    pluginManager.withPlugin("io.swagger.core.v3.swagger-gradle-plugin") {

        dependencies {
            // this is used to scan the classpath and generate an openapi yaml file
            implementation("io.swagger.core.v3:swagger-jaxrs2-jakarta:${swagger}")
            implementation("jakarta.ws.rs:jakarta.ws.rs-api:${rsApi}")
        }
        // this is used to scan the classpath and generate an openapi yaml file
        tasks.withType<io.swagger.v3.plugins.gradle.tasks.ResolveTask> {
            outputFileName = project.name
            outputFormat = io.swagger.v3.plugins.gradle.tasks.ResolveTask.Format.YAML
            sortOutput = true
            prettyPrint = true
            classpath = java.sourceSets["main"].runtimeClasspath
            buildClasspath = classpath
            resourcePackages = setOf("org.eclipse.dataspaceconnector")
            outputDir = file("${rootProject.projectDir.path}/resources/openapi/yaml")
        }
        configurations {
            all {
                exclude(group = "com.fasterxml.jackson.jaxrs", module = "jackson-jaxrs-json-provider")
            }
        }
    }

    tasks.test {
        useJUnitPlatform()
        testLogging {
            showStandardStreams = true
        }
    }

    if (System.getenv("JACOCO") == "true") {
        apply(plugin = "jacoco")
        tasks.test {
            finalizedBy(tasks.jacocoTestReport)
        }
        tasks.jacocoTestReport {
            reports {
                // Generate XML report for codecov.io
                xml.required.set(true)
            }
        }
    }
}
buildscript {
    dependencies {
        classpath("io.swagger.core.v3:swagger-gradle-plugin:2.1.13")
    }
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://oss.sonatype.org/content/repositories/snapshots/"))
            username.set(System.getenv("OSSRH_USER") ?: return@sonatype)
            password.set(System.getenv("OSSRH_PASSWORD") ?: return@sonatype)
        }
    }
}