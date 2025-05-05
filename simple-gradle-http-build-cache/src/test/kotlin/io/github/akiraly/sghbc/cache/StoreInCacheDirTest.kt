package io.github.akiraly.sghbc.cache

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.springframework.core.io.ByteArrayResource
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StoreInCacheDirTest {

    @TempDir
    lateinit var tempDir: Path

    private lateinit var cacheDirectory: CacheDirectory
    private lateinit var storeInCache: StoreInCacheDir
    private lateinit var cacheId: CacheId
    private lateinit var cacheKey: CacheKey
    private lateinit var cacheKeyDir: Path

    @BeforeEach
    fun setup() {
        cacheDirectory = CacheDirectory(tempDir.toString())
        storeInCache = StoreInCacheDir(cacheDirectory, 100) // 100MB max size
        cacheId = CacheId("1234567890abcdef1234567890abcdef")
        cacheKey = CacheKey("abcdef1234567890abcdef1234567890")
        cacheKeyDir = cacheDirectory.resolveDir(cacheId, cacheKey)
    }

    @Test
    fun `should store cache entry successfully`() {
        // Given
        val content = "Test cache content"
        val resource = ByteArrayResource(content.toByteArray())

        // When
        val result = storeInCache(cacheId, cacheKey, resource)

        // Then
        assertTrue(result, "Store operation should succeed")
        assertTrue(Files.exists(cacheKeyDir), "Cache key directory should exist")

        // Verify the latest symlink exists
        val latestLink = cacheKeyDir.resolve("latest")
        assertTrue(Files.exists(latestLink), "Latest symlink should exist")
        assertTrue(Files.isSymbolicLink(latestLink), "Latest should be a symlink")

        // Verify the content file exists and has the correct content
        val targetFile = cacheKeyDir.resolve(Files.readSymbolicLink(latestLink))
        assertTrue(Files.exists(targetFile), "Target file should exist")
        assertEquals(
            content,
            Files.readString(targetFile),
            "File content should match original content"
        )
    }

    @Test
    fun `should create cache directory if it doesn't exist`() {
        // Given
        val content = "Test cache content"
        val resource = ByteArrayResource(content.toByteArray())

        // Ensure directory doesn't exist yet
        Files.deleteIfExists(cacheKeyDir)
        assertFalse(Files.exists(cacheKeyDir), "Cache key directory should not exist initially")

        // When
        val result = storeInCache(cacheId, cacheKey, resource)

        // Then
        assertTrue(result, "Store operation should succeed")
        assertTrue(Files.exists(cacheKeyDir), "Cache key directory should be created")
    }

    @Test
    fun `should reuse existing file with same hash`() {
        // Given
        val content = "Test cache content"
        val resource1 = ByteArrayResource(content.toByteArray())
        val resource2 = ByteArrayResource(content.toByteArray())

        // When
        val result1 = storeInCache(cacheId, cacheKey, resource1)

        // Get the hash of the first file
        val latestLink = cacheKeyDir.resolve("latest")
        val firstFileHash = Files.readSymbolicLink(latestLink).toString()

        // Store the same content again
        val result2 = storeInCache(cacheId, cacheKey, resource2)

        // Then
        assertTrue(result1, "First store operation should succeed")
        assertTrue(result2, "Second store operation should succeed")

        // Verify the latest symlink still points to the same file
        val newLatestLink = cacheKeyDir.resolve("latest")
        val secondFileHash = Files.readSymbolicLink(newLatestLink).toString()

        assertEquals(
            firstFileHash,
            secondFileHash,
            "File hash should be the same for identical content"
        )

        // Count the number of files in the directory (should be 1 file + 1 symlink)
        val fileCount = Files.list(cacheKeyDir).count()
        assertEquals(2, fileCount, "Should have only one content file plus the symlink")
    }

    @Test
    fun `should update latest symlink when storing new content`() {
        // Given
        val content1 = "First cache content"
        val content2 = "Second cache content"
        val resource1 = ByteArrayResource(content1.toByteArray())
        val resource2 = ByteArrayResource(content2.toByteArray())

        // When
        storeInCache(cacheId, cacheKey, resource1)
        val latestLink = cacheKeyDir.resolve("latest")
        val firstFileHash = Files.readSymbolicLink(latestLink).toString()

        storeInCache(cacheId, cacheKey, resource2)
        val secondFileHash = Files.readSymbolicLink(latestLink).toString()

        // Then
        assertFalse(
            firstFileHash == secondFileHash,
            "File hashes should be different for different content"
        )

        // Verify both files exist
        assertTrue(
            Files.exists(cacheKeyDir.resolve(firstFileHash)),
            "First file should still exist"
        )
        assertTrue(Files.exists(cacheKeyDir.resolve(secondFileHash)), "Second file should exist")

        // Verify latest points to the second file
        val latestContent = Files.readString(cacheKeyDir.resolve(secondFileHash))
        assertEquals(content2, latestContent, "Latest should point to the second content")
    }

    @Test
    fun `should handle empty content`() {
        // Given
        val emptyContent = ByteArray(0)
        val resource = ByteArrayResource(emptyContent)

        // When
        val result = storeInCache(cacheId, cacheKey, resource)

        // Then
        assertTrue(result, "Store operation should succeed even with empty content")

        // Verify the latest symlink exists
        val latestLink = cacheKeyDir.resolve("latest")
        assertTrue(Files.exists(latestLink), "Latest symlink should exist")

        // Verify the content file exists and is empty
        val targetFile = cacheKeyDir.resolve(Files.readSymbolicLink(latestLink))
        assertTrue(Files.exists(targetFile), "Target file should exist")
        assertEquals(0, Files.size(targetFile), "File should be empty")
    }

    @Test
    fun `should handle large content within limits`() {
        // Given
        val largeContent = ByteArray(1024 * 1024) // 1MB
        val resource = ByteArrayResource(largeContent)

        // When
        val result = storeInCache(cacheId, cacheKey, resource)

        // Then
        assertTrue(result, "Store operation should succeed with large content within limits")

        // Verify the content file exists and has the correct size
        val latestLink = cacheKeyDir.resolve("latest")
        val targetFile = cacheKeyDir.resolve(Files.readSymbolicLink(latestLink))
        assertEquals(
            largeContent.size.toLong(),
            Files.size(targetFile),
            "File size should match content size"
        )
    }
}
