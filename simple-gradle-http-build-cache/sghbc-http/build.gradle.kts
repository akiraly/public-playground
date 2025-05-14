plugins {
    id("sghbc.library-conventions")
}

dependencies {
    implementation(project(":sghbc-domain"))

    implementation(platform(libs.spring.boot.dependencies))
    implementation(platform("org.jmolecules:jmolecules-bom:${libs.versions.jmolecules.bom.get()}"))

    implementation("commons-io:commons-io:${libs.versions.commons.io.get()}")

    implementation("org.jmolecules:kmolecules-ddd")
    implementation("com.fasterxml.jackson.core:jackson-annotations")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.slf4j:slf4j-api")

    implementation("org.springframework.boot:spring-boot-starter-web")
    runtimeOnly("org.springdoc:springdoc-openapi-starter-webmvc-ui:${libs.versions.springdoc.get()}")

    implementation(kotlin("stdlib"))

    testImplementation(kotlin("test"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(libs.mockk)
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
