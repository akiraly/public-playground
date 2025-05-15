plugins {
    id("sghbc.library-conventions")
}

dependencies {
    api(project(":sghbc-domain"))

    api(platform(libs.spring.boot.dependencies))
    implementation(platform(libs.spring.boot.dependencies))
    implementation(platform("org.jmolecules:jmolecules-bom:${libs.versions.jmolecules.bom.get()}"))

    implementation("commons-io:commons-io:${libs.versions.commons.io.get()}")

    runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.slf4j:slf4j-api")

    runtimeOnly("org.springdoc:springdoc-openapi-starter-webmvc-ui:${libs.versions.springdoc.get()}")

    implementation(kotlin("stdlib"))

    api("org.springframework:spring-beans")
    api("org.springframework:spring-context")
    implementation("org.springframework:spring-core")

    testImplementation(kotlin("test"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("io.mockk:mockk:${libs.versions.mockk.get()}")
    testImplementation("io.mockk:mockk-dsl:${libs.versions.mockk.get()}")
}
