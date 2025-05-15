plugins {
    id("sghbc.library-conventions")
}

dependencies {
    api(platform(libs.spring.boot.dependencies))
    implementation(platform(libs.spring.boot.dependencies))
    implementation(platform("org.jmolecules:jmolecules-bom:${libs.versions.jmolecules.bom.get()}"))

    api("org.jmolecules:kmolecules-ddd")
    implementation("com.fasterxml.jackson.core:jackson-annotations")
    runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin")

    implementation(kotlin("stdlib"))
    api("org.springframework:spring-core")

    testImplementation(kotlin("test"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("io.mockk:mockk:${libs.versions.mockk.get()}")
    testImplementation("io.mockk:mockk-dsl:${libs.versions.mockk.get()}")
}
