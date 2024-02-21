plugins {
  alias(libs.plugins.jvm)
  alias(libs.plugins.detekt)

  application
}

dependencies {
  implementation(libs.bundles.hoplite)
  implementation(libs.kotlinx.html)
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
  mainClass = "org.example.ContactsAppKt"
}
