package io.github.akiraly.sghbc.store.fs

import io.github.akiraly.sghbc.domain.CacheEntryId
import io.github.akiraly.sghbc.domain.CacheId
import io.github.akiraly.sghbc.domain.GradleCacheKey
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CacheDirectoryTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `should create cache directory if it does not exist`() {
        // Given
        val cacheDirectoryPath = tempDir.resolve("cache").toString()

        // When
        CacheDirectory(cacheDirectoryPath)

        // Then
        assertTrue(Files.exists(tempDir.resolve("cache")))
    }

    @Test
    fun `should use existing cache directory if it exists`() {
        // Given
        val cacheDirPath = tempDir.resolve("existing-cache")
        Files.createDirectories(cacheDirPath)

        // When
        CacheDirectory(cacheDirPath.toString())

        // Then
        assertTrue(Files.exists(cacheDirPath))
    }

    @Test
    fun `should resolve directory path correctly for cache entry id`() {
        // Given
        val cacheDirectory = CacheDirectory(tempDir.toString())
        val cacheId = CacheId("test-cache")
        val gradleCacheKey = GradleCacheKey("1234abcdef1234abcdef1234abcdef1234abcdef")
        val cacheEntryId = CacheEntryId(cacheId, gradleCacheKey)

        // When
        val resolvedDir = cacheDirectory.resolveDir(cacheEntryId)

        // Then
        val expectedPath =
            tempDir.resolve("test-cache").resolve("1234abcdef1234abcdef1234abcdef1234abcdef")
        assertEquals(expectedPath, resolvedDir)
    }

    @Test
    fun `should handle different cache ids and keys correctly`() {
        // Given
        val cacheDirectory = CacheDirectory(tempDir.toString())

        val cacheId1 = CacheId("cache-id1")
        val gradleCacheKey1 = GradleCacheKey("1111abcdef1111abcdef1111abcdef1111abcdef")
        val cacheEntryId1 = CacheEntryId(cacheId1, gradleCacheKey1)

        val cacheId2 = CacheId("cache-id2")
        val gradleCacheKey2 = GradleCacheKey("2222abcdef2222abcdef2222abcdef2222abcdef")
        val cacheEntryId2 = CacheEntryId(cacheId2, gradleCacheKey2)

        // When
        val resolvedDir1 = cacheDirectory.resolveDir(cacheEntryId1)
        val resolvedDir2 = cacheDirectory.resolveDir(cacheEntryId2)

        // Then
        val expectedPath1 =
            tempDir.resolve("cache-id1").resolve("1111abcdef1111abcdef1111abcdef1111abcdef")
        val expectedPath2 =
            tempDir.resolve("cache-id2").resolve("2222abcdef2222abcdef2222abcdef2222abcdef")

        assertEquals(expectedPath1, resolvedDir1)
        assertEquals(expectedPath2, resolvedDir2)
    }
}
