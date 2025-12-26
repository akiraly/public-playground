import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    jacoco
    id("io.gitlab.arturbosch.detekt")

    application
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

application {
    mainClass = "io.github.akiraly.sghbc.SghbcApplication"
}

dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting")

    implementation(platform(libs.spring.boot.dependencies))
    implementation(platform("org.jmolecules:jmolecules-bom:${libs.versions.jmolecules.bom.get()}"))

    runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin")

    implementation("org.springframework.boot:spring-boot-starter-web")
    //runtimeOnly("org.springdoc:springdoc-openapi-starter-webmvc-ui:${libs.versions.springdoc.get()}")

    implementation("org.springframework:spring-context")
    implementation("org.springframework.boot:spring-boot")
    implementation("org.springframework.boot:spring-boot-autoconfigure")

    testImplementation(kotlin("test"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("io.mockk:mockk:${libs.versions.mockk.get()}")
    testImplementation("org.hamcrest:hamcrest")
    testImplementation("org.springframework:spring-beans")
    testImplementation("org.springframework:spring-test")
    testImplementation("org.springframework.boot:spring-boot-test")
    testImplementation("org.springframework.boot:spring-boot-test-autoconfigure")
}
