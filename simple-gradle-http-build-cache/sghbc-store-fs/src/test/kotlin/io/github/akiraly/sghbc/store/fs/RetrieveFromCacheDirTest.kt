package io.github.akiraly.sghbc.store.fs

import io.github.akiraly.sghbc.domain.CacheEntryId
import io.github.akiraly.sghbc.domain.CacheId
import io.github.akiraly.sghbc.domain.GradleCacheKey
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.io.FileNotFoundException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RetrieveFromCacheDirTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `should retrieve cache entry when it exists`() {
        // Given
        val cacheId = CacheId("test-cache")
        val gradleCacheKey = GradleCacheKey("1234abcdef1234abcdef1234abcdef1234abcdef")
        val cacheEntryId = CacheEntryId(cacheId, gradleCacheKey)

        // Create cache directory structure
        val cacheKeyDir = tempDir.resolve(cacheId.value).resolve(gradleCacheKey.value)
        Files.createDirectories(cacheKeyDir)

        // Create a test file and symlink
        val testContent = "test content"
        val hashFile = cacheKeyDir.resolve("abcdef123456")
        Files.write(hashFile, testContent.toByteArray())

        val latestLink = cacheKeyDir.resolve("latest")
        Files.createSymbolicLink(latestLink, hashFile.fileName)

        val cacheDirectory = CacheDirectory(tempDir.toString())
        val retrieveFromCache = RetrieveFromCacheDir(cacheDirectory)

        // When
        val result = retrieveFromCache(cacheEntryId)

        // Then
        assertEquals(cacheEntryId, result.id)
        assertTrue(Files.isSameFile(hashFile, result.resource.file.toPath()))
    }

    @Test
    fun `should throw FileNotFoundException when cache directory does not exist`() {
        // Given
        val cacheId = CacheId("test-cache")
        val gradleCacheKey = GradleCacheKey("1234abcdef1234abcdef1234abcdef1234abcdef")
        val cacheEntryId = CacheEntryId(cacheId, gradleCacheKey)

        val cacheDirectory = CacheDirectory(tempDir.toString())
        val retrieveFromCache = RetrieveFromCacheDir(cacheDirectory)

        // When/Then
        assertThrows<FileNotFoundException> {
            retrieveFromCache(cacheEntryId)
        }
    }

    @Test
    fun `should throw FileNotFoundException when latest symlink does not exist`() {
        // Given
        val cacheId = CacheId("test-cache")
        val gradleCacheKey = GradleCacheKey("1234abcdef1234abcdef1234abcdef1234abcdef")
        val cacheEntryId = CacheEntryId(cacheId, gradleCacheKey)

        // Create cache directory structure without symlink
        val cacheKeyDir = tempDir.resolve(cacheId.value).resolve(gradleCacheKey.value)
        Files.createDirectories(cacheKeyDir)

        val cacheDirectory = CacheDirectory(tempDir.toString())
        val retrieveFromCache = RetrieveFromCacheDir(cacheDirectory)

        // When/Then
        assertThrows<FileNotFoundException> {
            retrieveFromCache(cacheEntryId)
        }
    }

    @Test
    fun `should throw FileNotFoundException when cache file does not exist`() {
        // Given
        val cacheId = CacheId("test-cache")
        val gradleCacheKey = GradleCacheKey("1234abcdef1234abcdef1234abcdef1234abcdef")
        val cacheEntryId = CacheEntryId(cacheId, gradleCacheKey)

        // Create cache directory structure
        val cacheKeyDir = tempDir.resolve(cacheId.value).resolve(gradleCacheKey.value)
        Files.createDirectories(cacheKeyDir)

        // Create a symlink to a non-existent file
        val nonExistentFile = cacheKeyDir.resolve("nonexistent")
        val latestLink = cacheKeyDir.resolve("latest")
        Files.createSymbolicLink(latestLink, nonExistentFile.fileName)

        val cacheDirectory = CacheDirectory(tempDir.toString())
        val retrieveFromCache = RetrieveFromCacheDir(cacheDirectory)

        // When/Then
        assertThrows<FileNotFoundException> {
            retrieveFromCache(cacheEntryId)
        }
    }

    @Test
    fun `should use cache directory to resolve paths`() {
        // Given
        val cacheId = CacheId("test-cache")
        val gradleCacheKey = GradleCacheKey("1234abcdef1234abcdef1234abcdef1234abcdef")
        val cacheEntryId = CacheEntryId(cacheId, gradleCacheKey)

        val cacheDirectory = mockk<CacheDirectory>()
        val cacheKeyDir = tempDir.resolve("mocked-path")
        Files.createDirectories(cacheKeyDir)

        // Create a test file and symlink
        val testContent = "test content"
        val hashFile = cacheKeyDir.resolve("abcdef123456")
        Files.write(hashFile, testContent.toByteArray())

        val latestLink = cacheKeyDir.resolve("latest")
        Files.createSymbolicLink(latestLink, hashFile.fileName)

        every { cacheDirectory.resolveDir(cacheEntryId) } returns cacheKeyDir

        val retrieveFromCache = RetrieveFromCacheDir(cacheDirectory)

        // When
        val result = retrieveFromCache(cacheEntryId)

        // Then
        verify { cacheDirectory.resolveDir(cacheEntryId) }
        assertEquals(cacheEntryId, result.id)
        assertTrue(Files.isSameFile(hashFile, result.resource.file.toPath()))
    }
}
