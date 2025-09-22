allprojects {
    group = "io.github.akiraly.contacts-htmx-springmvc"
    version = "0.0.1-SNAPSHOT"
}

tasks.wrapper {
    gradleVersion = libs.versions.gradle.get()
    distributionType = Wrapper.DistributionType.ALL
}
