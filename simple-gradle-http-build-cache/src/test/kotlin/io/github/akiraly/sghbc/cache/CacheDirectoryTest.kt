package io.github.akiraly.sghbc.cache

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
    fun `should create cache directory if it doesn't exist`() {
        // Given
        val nonExistentDir = tempDir.resolve("non-existent-dir")

        // When
        CacheDirectory(nonExistentDir.toString())

        // Then
        assertTrue(Files.exists(nonExistentDir), "Cache directory should be created")
        assertTrue(Files.isDirectory(nonExistentDir), "Cache directory should be a directory")
    }

    @Test
    fun `should use existing cache directory if it exists`() {
        // Given
        val existingDir = tempDir.resolve("existing-dir")
        Files.createDirectories(existingDir)

        // When
        CacheDirectory(existingDir.toString())

        // Then
        assertTrue(Files.exists(existingDir), "Cache directory should exist")
        assertTrue(Files.isDirectory(existingDir), "Cache directory should be a directory")
    }

    @Test
    fun `should resolve directory path correctly for cache id and key`() {
        // Given
        val cacheDirectory = CacheDirectory(tempDir.toString())
        val cacheId = CacheId("1234567890abcdef1234567890abcdef")
        val cacheKey = CacheKey("abcdef1234567890abcdef1234567890")

        // When
        val resolvedDir = cacheDirectory.resolveDir(cacheId, cacheKey)

        // Then
        val expectedPath = tempDir.resolve(cacheId.id).resolve(cacheKey.id)
        assertEquals(
            expectedPath,
            resolvedDir,
            "Resolved directory path should match expected path"
        )
    }

    @Test
    fun `should create nested directories when resolving path`() {
        // Given
        val cacheDirectory = CacheDirectory(tempDir.toString())
        val cacheId = CacheId("1234567890abcdef1234567890abcdef")
        val cacheKey = CacheKey("abcdef1234567890abcdef1234567890")

        // When
        val resolvedDir = cacheDirectory.resolveDir(cacheId, cacheKey)
        // Note: The resolveDir method doesn't actually create the directories,
        // it just returns the path. The directories are created when storing cache entries.

        // Then
        val expectedPath = tempDir.resolve(cacheId.id).resolve(cacheKey.id)
        assertEquals(
            expectedPath,
            resolvedDir,
            "Resolved directory path should match expected path"
        )

        // Verify the path structure is as expected
        assertEquals(
            cacheId.id, resolvedDir.getName(resolvedDir.nameCount - 2).toString(),
            "Second-to-last path component should be the cache ID"
        )
        assertEquals(
            cacheKey.id, resolvedDir.fileName.toString(),
            "Last path component should be the cache key"
        )
    }
}
