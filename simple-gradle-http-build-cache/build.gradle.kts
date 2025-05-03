import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    jacoco
    application
    alias(libs.plugins.detekt)
}

group = "io.github.akiraly"
version = "0.0.1-SNAPSHOT"

tasks.wrapper {
    gradleVersion = libs.versions.gradle.get()
    distributionType = Wrapper.DistributionType.ALL
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
    withSourcesJar()
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.set(
            listOf(
                "-Xjsr305=strict",
                "-Xjspecify-annotations=strict",
                "-Xtype-enhancement-improvements-strict-mode",
                "-Xjvm-default=all",
                "-Xconsistent-data-class-copy-visibility",
                //"-Xsuppress-warning=UNUSED_ANONYMOUS_PARAMETER" // should be fixed in 2.1.20
            )
        )
        javaParameters = true
        progressiveMode = true
        allWarningsAsErrors = false // Don't treat warnings as errors for tests
        extraWarnings = true
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    finalizedBy(tasks.named("jacocoTestReport"))
}

tasks.jacocoTestReport {
    dependsOn(tasks.named("test"))
    reports {
        xml.required = true
        html.required = true
    }
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.8".toBigDecimal()
            }
        }
    }
}

// Set up task configurations
tasks.withType<Detekt>().configureEach {
    reports {
        html.required.set(true) // Generate HTML report
        xml.required.set(true) // Generate XML report
        txt.required.set(true) // Generate TXT report
        sarif.required.set(true) // Generate SARIF report
    }
}

// Add detekt to the check task
tasks.named("check") {
    dependsOn(tasks.withType<Detekt>())
}

// Dependencies needed by detekt
dependencies {
    detektPlugins(libs.detekt.formatting)
}


application {
    mainClass = "io.github.akiraly.sghbc.SimpleGradleHTTPBuildCacheApplicationKt"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform(libs.spring.boot.dependencies))
    implementation(platform("org.jmolecules:jmolecules-bom:${libs.versions.jmolecules.bom.get()}"))

    implementation("org.jmolecules:kmolecules-ddd")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.slf4j:slf4j-api")

    implementation("org.springframework.boot:spring-boot-starter-web")

    implementation(kotlin("stdlib"))

    testImplementation(kotlin("test"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(libs.mockk)
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(module = "mockito-core")
    }
}
