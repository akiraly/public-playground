import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("plugin.spring")

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
                "-Xjvm-default=all",
                "-Xconsistent-data-class-copy-visibility",
                "-Xsuppress-warning=UNUSED_ANONYMOUS_PARAMETER" // should be fixed in 2.1.20
            )
        )
        javaParameters = true
        allWarningsAsErrors = true
        extraWarnings = true
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

application {
    mainClass = "org.example.AppKt"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform(libs.spring.boot.dependencies))

    implementation("org.springframework.boot:spring-boot-starter-web")

    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:${libs.versions.kotlinx.html.get()}")
    implementation("org.jetbrains.kotlinx:kotlinx-html:${libs.versions.kotlinx.html.get()}")

    runtimeOnly("org.webjars.npm:htmx.org:${libs.versions.htmx.get()}")

    testImplementation(kotlin("test"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
