package io.github.akiraly.sghbc.store.fs

import io.github.akiraly.sghbc.domain.CacheEntry
import io.github.akiraly.sghbc.domain.CacheEntryId
import io.github.akiraly.sghbc.domain.CacheId
import io.github.akiraly.sghbc.domain.GradleCacheKey
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.springframework.core.io.ByteArrayResource
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StoreInCacheDirTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `should store cache entry successfully`() {
        // Given
        val cacheId = CacheId("test-cache")
        val gradleCacheKey = GradleCacheKey("1234abcdef")
        val cacheEntryId = CacheEntryId(cacheId, gradleCacheKey)
        val cacheContent = "test cache content"
        val resource = ByteArrayResource(cacheContent.toByteArray())
        val cacheEntry = CacheEntry(cacheEntryId, resource)

        val cacheDirectory = CacheDirectory(tempDir.toString())
        val storeInCache = StoreInCacheDir(cacheDirectory, 100)

        // When
        val result = storeInCache(cacheEntryId, cacheEntry)

        // Then
        assertTrue(result)

        // Verify directory structure
        val cacheKeyDir = tempDir.resolve(cacheId.value).resolve(gradleCacheKey.value)
        assertTrue(Files.exists(cacheKeyDir))

        // Verify latest symlink exists
        val latestLink = cacheKeyDir.resolve("latest")
        assertTrue(Files.exists(latestLink))
        assertTrue(Files.isSymbolicLink(latestLink))

        // Verify content was stored with hash filename
        val hashFilePath = cacheKeyDir.resolve(Files.readSymbolicLink(latestLink))
        assertTrue(Files.exists(hashFilePath))

        // Verify content
        val storedContent = Files.readAllBytes(hashFilePath)
        assertEquals(cacheContent, String(storedContent))
    }

    @Test
    fun `should create cache directory if it does not exist`() {
        // Given
        val cacheId = CacheId("test-cache")
        val gradleCacheKey = GradleCacheKey("1234abcdef")
        val cacheEntryId = CacheEntryId(cacheId, gradleCacheKey)
        val cacheContent = "test cache content"
        val resource = ByteArrayResource(cacheContent.toByteArray())
        val cacheEntry = CacheEntry(cacheEntryId, resource)

        val cacheDirectory = CacheDirectory(tempDir.toString())
        val storeInCache = StoreInCacheDir(cacheDirectory, 100)

        // When
        val result = storeInCache(cacheEntryId, cacheEntry)

        // Then
        assertTrue(result)

        // Verify directory was created
        val cacheKeyDir = tempDir.resolve(cacheId.value).resolve(gradleCacheKey.value)
        assertTrue(Files.exists(cacheKeyDir))
    }

    @Test
    fun `should not create duplicate files when hash already exists`() {
        // Given
        val cacheId = CacheId("test-cache")
        val gradleCacheKey = GradleCacheKey("1234abcdef")
        val cacheEntryId = CacheEntryId(cacheId, gradleCacheKey)
        val cacheContent = "test cache content"
        val resource = ByteArrayResource(cacheContent.toByteArray())
        val cacheEntry = CacheEntry(cacheEntryId, resource)

        val cacheDirectory = CacheDirectory(tempDir.toString())
        val storeInCache = StoreInCacheDir(cacheDirectory, 100)

        // Store the entry once
        storeInCache(cacheEntryId, cacheEntry)

        // Get the hash file path
        val cacheKeyDir = tempDir.resolve(cacheId.value).resolve(gradleCacheKey.value)
        val latestLink = cacheKeyDir.resolve("latest")
        val hashFilePath = cacheKeyDir.resolve(Files.readSymbolicLink(latestLink))

        // Record the last modified time
        val firstModifiedTime = Files.getLastModifiedTime(hashFilePath)

        // Wait a bit to ensure the timestamp would be different if the file is recreated
        Thread.sleep(100)

        // When - Store the same entry again
        val result = storeInCache(cacheEntryId, cacheEntry)

        // Then
        assertTrue(result)

        // Verify the file wasn't recreated (same modified time)
        val secondModifiedTime = Files.getLastModifiedTime(hashFilePath)
        assertEquals(firstModifiedTime, secondModifiedTime)
    }

    @Test
    fun `should update latest symlink when storing new content`() {
        // Given
        val cacheId = CacheId("test-cache")
        val gradleCacheKey = GradleCacheKey("1234abcdef")
        val cacheEntryId = CacheEntryId(cacheId, gradleCacheKey)

        // First content
        val cacheContent1 = "test cache content 1"
        val resource1 = ByteArrayResource(cacheContent1.toByteArray())
        val cacheEntry1 = CacheEntry(cacheEntryId, resource1)

        // Second content (different)
        val cacheContent2 = "test cache content 2"
        val resource2 = ByteArrayResource(cacheContent2.toByteArray())
        val cacheEntry2 = CacheEntry(cacheEntryId, resource2)

        val cacheDirectory = CacheDirectory(tempDir.toString())
        val storeInCache = StoreInCacheDir(cacheDirectory, 100)

        // Store the first entry
        storeInCache(cacheEntryId, cacheEntry1)

        // Get the first hash file path
        val cacheKeyDir = tempDir.resolve(cacheId.value).resolve(gradleCacheKey.value)
        val latestLink = cacheKeyDir.resolve("latest")
        val hashFilePath1 = cacheKeyDir.resolve(Files.readSymbolicLink(latestLink))

        // When - Store the second entry
        val result = storeInCache(cacheEntryId, cacheEntry2)

        // Then
        assertTrue(result)

        // Verify latest symlink points to a different file
        val hashFilePath2 = cacheKeyDir.resolve(Files.readSymbolicLink(latestLink))
        assertFalse(Files.isSameFile(hashFilePath1, hashFilePath2))

        // Verify both files exist
        assertTrue(Files.exists(hashFilePath1))
        assertTrue(Files.exists(hashFilePath2))

        // Verify content of both files
        assertEquals(cacheContent1, String(Files.readAllBytes(hashFilePath1)))
        assertEquals(cacheContent2, String(Files.readAllBytes(hashFilePath2)))
    }

    @Test
    fun `should return false when an exception occurs`() {
        // Given
        val cacheId = CacheId("test-cache")
        val gradleCacheKey = GradleCacheKey("1234abcdef")
        val cacheEntryId = CacheEntryId(cacheId, gradleCacheKey)

        // Create a resource that throws an exception when read
        val resource = mockk<ByteArrayResource>()
        every { resource.inputStream } throws IOException("Simulated error")

        val cacheEntry = CacheEntry(cacheEntryId, resource)

        val cacheDirectory = CacheDirectory(tempDir.toString())
        val storeInCache = StoreInCacheDir(cacheDirectory, 100)

        // When
        val result = storeInCache(cacheEntryId, cacheEntry)

        // Then
        assertFalse(result)
        verify { resource.inputStream }
    }

    @Test
    fun `should use cache directory to resolve paths`() {
        // Given
        val cacheId = CacheId("test-cache")
        val gradleCacheKey = GradleCacheKey("1234abcdef")
        val cacheEntryId = CacheEntryId(cacheId, gradleCacheKey)
        val cacheContent = "test cache content"
        val resource = ByteArrayResource(cacheContent.toByteArray())
        val cacheEntry = CacheEntry(cacheEntryId, resource)

        val cacheDirectory = mockk<CacheDirectory>()
        val cacheKeyDir = tempDir.resolve("mocked-path")
        Files.createDirectories(cacheKeyDir)

        every { cacheDirectory.resolveDir(cacheEntryId) } returns cacheKeyDir

        val storeInCache = StoreInCacheDir(cacheDirectory, 100)

        // When
        val result = storeInCache(cacheEntryId, cacheEntry)

        // Then
        assertTrue(result)
        verify { cacheDirectory.resolveDir(cacheEntryId) }

        // Verify latest symlink exists in the mocked path
        val latestLink = cacheKeyDir.resolve("latest")
        assertTrue(Files.exists(latestLink))
    }
}
