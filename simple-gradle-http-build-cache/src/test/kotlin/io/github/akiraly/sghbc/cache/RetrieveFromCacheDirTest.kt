package io.github.akiraly.sghbc.cache

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.FileNotFoundException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class RetrieveFromCacheDirTest {

    @TempDir
    lateinit var tempDir: Path

    private lateinit var cacheDirectory: CacheDirectory
    private lateinit var retrieveFromCache: RetrieveFromCacheDir
    private lateinit var cacheId: CacheId
    private lateinit var cacheKey: CacheKey
    private lateinit var cacheKeyDir: Path
    private lateinit var contentFile: Path

    @BeforeEach
    fun setup() {
        cacheDirectory = CacheDirectory(tempDir.toString())
        retrieveFromCache = RetrieveFromCacheDir(cacheDirectory)
        cacheId = CacheId("1234567890abcdef1234567890abcdef")
        cacheKey = CacheKey("abcdef1234567890abcdef1234567890")
        cacheKeyDir = cacheDirectory.resolveDir(cacheId, cacheKey)
        Files.createDirectories(cacheKeyDir)
    }

    @Test
    fun `should retrieve cache entry when it exists`() {
        // Given
        val content = "Test cache content"
        val contentHash = "testhash123"
        contentFile = cacheKeyDir.resolve(contentHash)
        Files.write(contentFile, content.toByteArray())

        // Create the "latest" symlink
        val latestLink = cacheKeyDir.resolve("latest")
        Files.createSymbolicLink(latestLink, contentFile.fileName)

        // When
        val resource = retrieveFromCache(cacheId, cacheKey)

        // Then
        assertTrue(resource.exists(), "Resource should exist")
        assertEquals(content, resource.inputStream.bufferedReader().use { it.readText() },
            "Retrieved content should match original content")
    }

    @Test
    fun `should throw FileNotFoundException when cache directory doesn't exist`() {
        // Given
        val nonExistentCacheId = CacheId("aaaabbbbccccddddeeeeffffaaaabbbb")

        // When/Then
        assertFailsWith<FileNotFoundException> {
            retrieveFromCache(nonExistentCacheId, cacheKey)
        }
    }

    @Test
    fun `should throw FileNotFoundException when latest symlink doesn't exist`() {
        // Given
        // Cache directory exists but no "latest" symlink

        // When/Then
        assertFailsWith<FileNotFoundException> {
            retrieveFromCache(cacheId, cacheKey)
        }
    }

    @Test
    fun `should throw FileNotFoundException when target file doesn't exist`() {
        // Given
        // Create the "latest" symlink pointing to a non-existent file
        val nonExistentFile = cacheKeyDir.resolve("nonexistent")
        val latestLink = cacheKeyDir.resolve("latest")
        Files.createSymbolicLink(latestLink, nonExistentFile.fileName)

        // When/Then
        assertFailsWith<FileNotFoundException> {
            retrieveFromCache(cacheId, cacheKey)
        }
    }

    @Test
    fun `should follow latest symlink to get the most recent cache entry`() {
        // Given
        // Create an old cache entry
        val oldContent = "Old cache content"
        val oldContentHash = "oldhash123"
        val oldContentFile = cacheKeyDir.resolve(oldContentHash)
        Files.write(oldContentFile, oldContent.toByteArray())

        // Create a new cache entry
        val newContent = "New cache content"
        val newContentHash = "newhash456"
        val newContentFile = cacheKeyDir.resolve(newContentHash)
        Files.write(newContentFile, newContent.toByteArray())

        // Create the "latest" symlink pointing to the new file
        val latestLink = cacheKeyDir.resolve("latest")
        Files.createSymbolicLink(latestLink, newContentFile.fileName)

        // When
        val resource = retrieveFromCache(cacheId, cacheKey)

        // Then
        assertTrue(resource.exists(), "Resource should exist")
        assertEquals(newContent, resource.inputStream.bufferedReader().use { it.readText() },
            "Retrieved content should match the new content")
    }
}
