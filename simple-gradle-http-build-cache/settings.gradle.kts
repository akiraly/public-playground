rootProject.name = "simple-gradle-http-build-cache"

pluginManagement {
    repositories {
        gradlePluginPortal()
    }
    plugins {
        val foojayResolver: String by settings
        id("org.gradle.toolchains.foojay-resolver-convention") version foojayResolver

        val buildHealth: String by settings
        id("com.autonomousapps.build-health") version buildHealth

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

include(
    "sghbc",
    "sghbc-bom",
    "sghbc-domain",
    "sghbc-http",
    "sghbc-integration-tests",
    "sghbc-store-fs"
)
