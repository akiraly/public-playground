allprojects {
    group = "io.github.akiraly.sghbc"
    version = "0.0.1-SNAPSHOT"
}

tasks.wrapper {
    gradleVersion = libs.versions.gradle.get()
    distributionType = Wrapper.DistributionType.ALL
}
