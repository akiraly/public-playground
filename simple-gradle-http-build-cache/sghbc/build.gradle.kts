plugins {
    id("sghbc.kotlin-conventions")
    application
}

application {
    mainClass = "io.github.akiraly.sghbc.SghbcApplication"
}

dependencies {
    runtimeOnly(project(":sghbc-domain"))
    runtimeOnly(project(":sghbc-http"))
    runtimeOnly(project(":sghbc-store-fs"))

    implementation(platform(libs.spring.boot.dependencies))
    implementation(platform("org.jmolecules:jmolecules-bom:${libs.versions.jmolecules.bom.get()}"))

    runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin")

    implementation("org.springframework.boot:spring-boot-starter-web")
    runtimeOnly("org.springdoc:springdoc-openapi-starter-webmvc-ui:${libs.versions.springdoc.get()}")

    implementation("org.springframework:spring-context")
    implementation("org.springframework.boot:spring-boot")
    implementation("org.springframework.boot:spring-boot-autoconfigure")

    implementation(kotlin("stdlib"))

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
