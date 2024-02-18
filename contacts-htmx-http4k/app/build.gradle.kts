plugins {
  alias(libs.plugins.jvm)

  application
}

dependencies {
  implementation(libs.guava)
  implementation(libs.logback)

  testImplementation(libs.bundles.kotest)
}

tasks.withType<Test>().configureEach {
  useJUnitPlatform()
}

kotlin {
  jvmToolchain(21)
}

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(21)
  }
}

application {
  mainClass = "org.example.AppKt"
}
