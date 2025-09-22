import org.codehaus.groovy.tools.shell.util.Logger.io

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
    val foojayResolver: String by settings
  id("org.gradle.toolchains.foojay-resolver-convention") version foojayResolver

    val buildHealth: String by settings
  id("com.autonomousapps.build-health") version buildHealth

  kotlin("jvm") apply false
  kotlin("plugin.spring") apply false

    io.gitlab.arturbosch.detekt:io.gitlab.arturbosch.detekt.gradle.plugin:${libs.versions.detekt.get()}
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
