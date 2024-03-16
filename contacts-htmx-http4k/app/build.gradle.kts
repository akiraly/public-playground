plugins {
  alias(libs.plugins.jvm)
  alias(libs.plugins.detekt)

  application
}

dependencies {
  implementation(platform(libs.kotlin.bom))

  implementation(platform(libs.http4k.bom))
  implementation(libs.bundles.http4k)

  implementation(libs.bundles.hoplite)
  implementation(libs.kotlinx.html)
  implementation(libs.logback)

  implementation(libs.swagger.ui)

  testImplementation(platform(libs.kotest.bom))
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
