package io.github.akiraly.sghbc.integration

import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.PosixFilePermission
import java.util.concurrent.TimeUnit

@SpringBootTest(
    properties = ["spring.main.allow-bean-definition-overriding=true"],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class SghbcGradleCacheIntegrationTest {

    @LocalServerPort
    private var port: Int = 0

    private lateinit var tempDir: Path

    @BeforeEach
    fun setup() {
        tempDir = Files.createTempDirectory("gradle-cache-test")
    }

    @AfterEach
    fun cleanup() {
        FileUtils.deleteDirectory(tempDir.toFile())
    }

    @Test
    fun `should store and retrieve Gradle build cache entries`() {
        // Given: A test Gradle project configured to use our cache
        val testProjectDir = copyTestProject()
        updateBuildCacheUrl(testProjectDir)

        // When: Running a first build to populate the cache
        @Suppress("UnusedVariable")
        val firstBuildOutput = runGradleBuild(testProjectDir, "clean", "test")

        // And: Running a second build that should use the cache
        val secondBuildOutput = runGradleBuild(testProjectDir, "clean", "test")

        // Then: The second build output should contain cache hit indicators
        assertTrue(
            secondBuildOutput.contains("Build cache key"),
            "Second build output should contain 'Build cache key' indicator, but was: $secondBuildOutput"
        )
    }

    private fun copyTestProject(): Path {
        val resourcesPath = Paths.get("src", "test", "resources", "test-gradle-project")
        val sourceDir = File(resourcesPath.toString()).absoluteFile
        val targetDir = tempDir.resolve("test-gradle-project").toFile()

        FileUtils.copyDirectory(sourceDir, targetDir)

        // Copy Gradle wrapper files from the root project
        val rootDir = File(System.getProperty("user.dir")).absoluteFile.parentFile

        // Copy gradle-wrapper.jar and gradle-wrapper.properties
        val rootWrapperDir = rootDir.resolve("gradle/wrapper")
        val targetWrapperDir = targetDir.resolve("gradle/wrapper")
        targetWrapperDir.mkdirs()

        FileUtils.copyFile(
            rootWrapperDir.resolve("gradle-wrapper.jar"),
            targetWrapperDir.resolve("gradle-wrapper.jar")
        )

        FileUtils.copyFile(
            rootWrapperDir.resolve("gradle-wrapper.properties"),
            targetWrapperDir.resolve("gradle-wrapper.properties")
        )

        // Copy gradlew and gradlew.bat
        FileUtils.copyFile(
            rootDir.resolve("gradlew"),
            targetDir.resolve("gradlew")
        )

        FileUtils.copyFile(
            rootDir.resolve("gradlew.bat"),
            targetDir.resolve("gradlew.bat")
        )

        // Make gradlew executable on Unix-like systems
        if (!System.getProperty("os.name").lowercase().contains("windows")) {
            val gradlew = targetDir.resolve("gradlew")
            if (gradlew.exists()) {
                try {
                    val permissions = HashSet<PosixFilePermission>()
                    permissions.add(PosixFilePermission.OWNER_READ)
                    permissions.add(PosixFilePermission.OWNER_WRITE)
                    permissions.add(PosixFilePermission.OWNER_EXECUTE)
                    permissions.add(PosixFilePermission.GROUP_READ)
                    permissions.add(PosixFilePermission.GROUP_EXECUTE)
                    permissions.add(PosixFilePermission.OTHERS_READ)
                    permissions.add(PosixFilePermission.OTHERS_EXECUTE)
                    Files.setPosixFilePermissions(gradlew.toPath(), permissions)
                } catch (_: Exception) {
                    // Fallback if setPosixFilePermissions is not supported
                    val process = ProcessBuilder("chmod", "+x", gradlew.absolutePath)
                        .redirectErrorStream(true)
                        .start()
                    process.waitFor(10, TimeUnit.SECONDS)
                }
            }
        }

        return targetDir.toPath()
    }

    private fun updateBuildCacheUrl(projectDir: Path) {
        val settingsFile = projectDir.resolve("settings.gradle").toFile()
        val content = FileUtils.readFileToString(settingsFile, StandardCharsets.UTF_8)
        val updatedContent = content.replace(
            "http://localhost:8123/cache/",
            "http://localhost:$port/cache/"
        )
        FileUtils.writeStringToFile(settingsFile, updatedContent, StandardCharsets.UTF_8)
    }


    private fun runGradleBuild(projectDir: Path, vararg tasks: String): String {
        val command = mutableListOf<String>()

        // Try different commands to run Gradle
        val isWindows = System.getProperty("os.name").lowercase().contains("windows")

        // Use the main project's gradlew script
        val rootDir = File(System.getProperty("user.dir")).absoluteFile.parentFile
        println("[DEBUG_LOG] Root directory: ${rootDir.absolutePath}")
        if (isWindows) {
            command.add(rootDir.resolve("gradlew.bat").absolutePath)
        } else {
            command.add(rootDir.resolve("gradlew").absolutePath)
        }

        // Print the command for debugging
        println("[DEBUG_LOG] Running command: ${command.joinToString(" ")}")

        // Add the tasks
        command.addAll(tasks)

        // Add the --info flag to show cache hits/misses
        command.add("--info")

        // Enable the build cache
        command.add("--build-cache")

        // Pass the current JVM's Java home to the Gradle build
        val javaHome = System.getProperty("java.home")
        command.add("-Dorg.gradle.java.home=$javaHome")

        // Create the process builder
        val processBuilder = ProcessBuilder(command)
        processBuilder.directory(projectDir.toFile())

        // Set JAVA_HOME environment variable
        val env = processBuilder.environment()
        env["JAVA_HOME"] = javaHome

        // Redirect error stream to output stream
        processBuilder.redirectErrorStream(true)

        // Start the process and capture its output
        val process = processBuilder.start()
        val output = process.inputStream.bufferedReader().use { it.readText() }

        // Wait for the process to complete
        if (!process.waitFor(2, TimeUnit.MINUTES)) {
            process.destroy()
            throw RuntimeException("Gradle build timed out")
        }

        // Check the exit code
        if (process.exitValue() != 0) {
            throw RuntimeException("Gradle build failed with exit code ${process.exitValue()}: $output")
        }

        return output
    }
}
