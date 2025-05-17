plugins {
    id("sghbc.kotlin-conventions")
}

dependencies {
    testImplementation(project(":sghbc"))

    testImplementation(platform(libs.spring.boot.dependencies))

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-test")
    testImplementation("org.springframework.boot:spring-boot-test-autoconfigure")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("io.mockk:mockk:${libs.versions.mockk.get()}")

    testImplementation("commons-io:commons-io:${libs.versions.commons.io.get()}")
}
