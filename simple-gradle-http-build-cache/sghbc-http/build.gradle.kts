plugins {
    id("sghbc.library-conventions")
}

dependencies {
    api(project(":sghbc-domain"))

    api(platform(libs.spring.boot.dependencies))
    implementation(platform(libs.spring.boot.dependencies))
    implementation(platform("org.jmolecules:jmolecules-bom:${libs.versions.jmolecules.bom.get()}"))

    runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.slf4j:slf4j-api")

    implementation("org.springframework.boot:spring-boot-starter-web")
    runtimeOnly("org.springdoc:springdoc-openapi-starter-webmvc-ui:${libs.versions.springdoc.get()}")

    api("org.springframework:spring-beans")
    api("org.springframework:spring-core")
    api("org.springframework:spring-web")

    testImplementation(kotlin("test"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("io.mockk:mockk:${libs.versions.mockk.get()}")
    testImplementation("io.mockk:mockk-dsl:${libs.versions.mockk.get()}")
    testImplementation("org.springframework:spring-test")
}
