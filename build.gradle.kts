plugins {
    java
    `java-library`
}

val edcGroup: String by project
val annotationProcessorVersion: String by project
val javaVersion: String by project
val metaModelVersion: String by project

// these values are required for the project POM (for publishing)
val edcScmConnection: String by project
val edcWebsiteUrl: String by project
val edcScmUrl: String by project

val defaultVersion: String by project
// makes the project version overridable using the "-PregSrvVersion=..." flag. Useful for CI builds
var actualVersion: String = (project.findProperty("version") ?: defaultVersion) as String
if (actualVersion == "unspecified") {
    actualVersion = defaultVersion
}

allprojects {

    apply(plugin = "${edcGroup}.edc-build")


    // configure which version of the annotation processor to use. defaults to the same version as the plugin
    configure<org.eclipse.edc.plugins.autodoc.AutodocExtension> {
        processorVersion.set(annotationProcessorVersion)
        outputDirectory.set(project.buildDir)
    }

    configure<org.eclipse.edc.plugins.edcbuild.extensions.BuildExtension> {
        versions {
            // override default dependency versions here
            projectVersion.set(actualVersion)
            metaModel.set(metaModelVersion)

        }
        pom {
            projectName.set(project.name)
            description.set("edc :: ${project.name}")
            projectUrl.set(edcWebsiteUrl)
            scmConnection.set(edcScmConnection)
            scmUrl.set(edcScmUrl)
        }
        swagger {
            title.set("Registration Service REST API")
            description = "Registration Service REST APIs - merged by OpenApiMerger"
            outputFilename.set(project.name)
            outputDirectory.set(file("${rootProject.projectDir.path}/resources/openapi/yaml"))
        }
        javaLanguageVersion.set(JavaLanguageVersion.of(javaVersion))
    }

    configure<CheckstyleExtension> {
        configFile = rootProject.file("resources/checkstyle-config.xml")
        configDirectory.set(rootProject.file("resources"))
    }


    // EdcRuntimeExtension uses this to determine the runtime classpath of the module to run.
    tasks.register("printClasspath") {
        doLast {
            println(sourceSets["main"].runtimeClasspath.asPath)
        }
    }
}
buildscript {
    dependencies {
        val edcGradlePluginsVersion: String by project
        classpath("org.eclipse.edc.edc-build:org.eclipse.edc.edc-build.gradle.plugin:${edcGradlePluginsVersion}")
    }
}
repositories {
    mavenCentral()
}
