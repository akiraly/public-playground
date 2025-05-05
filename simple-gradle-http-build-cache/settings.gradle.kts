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

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention")

    id("com.autonomousapps.build-health")

    kotlin("jvm") apply false
    kotlin("plugin.spring") apply false
}
