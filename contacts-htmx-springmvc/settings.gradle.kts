pluginManagement {
    repositories {
        gradlePluginPortal()
    }
    plugins {
        val foojayResolver: String by settings
        id("org.gradle.toolchains.foojay-resolver-convention") version foojayResolver

        val buildHealth: String by settings
        id("com.autonomousapps.build-health") version buildHealth

        val detekt: String by settings
        id("io.gitlab.arturbosch.detekt") version detekt

        val kotlinVersion: String by settings
        kotlin("jvm") version kotlinVersion
        kotlin("plugin.spring") version kotlinVersion
    }
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention")

    id("com.autonomousapps.build-health")

    kotlin("jvm") apply false
    kotlin("plugin.spring") apply false
}

dependencyAnalysis {
    issues {
        all {
            onAny {
                severity("fail")
            }

            onUnusedDependencies {
                exclude("org.springframework.boot:spring-boot-starter")
                exclude("org.springframework.boot:spring-boot-starter-test")
                exclude("org.springframework.boot:spring-boot-starter-web")

                exclude("org.jetbrains.kotlin:kotlin-test")
            }
        }
    }
}

rootProject.name = "contacts-htmx-springmvc"
include("app")
