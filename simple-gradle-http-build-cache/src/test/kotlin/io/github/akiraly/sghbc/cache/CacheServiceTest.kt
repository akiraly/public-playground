package io.github.akiraly.sghbc.cache

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayInputStream
import java.io.FileNotFoundException
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest

class CacheServiceTest {

    @TempDir
    lateinit var tempDir: Path

    private lateinit var storeInCache: StoreInCacheDir
    private lateinit var retrieveFromCache: RetrieveFromCacheDir
    private lateinit var cacheDirectory: CacheDirectory

    @BeforeEach
    fun setUp() {
        cacheDirectory = CacheDirectory(tempDir.toString())
        storeInCache = StoreInCacheDir(cacheDirectory)
        retrieveFromCache = RetrieveFromCacheDir(cacheDirectory)
    }

    @Test
    fun `should store cache entry successfully with SHA-256 hash filename`() {
        // Given
        val cacheId = CacheId("test-cache")
        val cacheKey = CacheKey("test-key")
        val content = "test content"
        val inputStream = ByteArrayInputStream(content.toByteArray())

        // Calculate expected SHA-256 hash
        val expectedHash = calculateSha256(content.toByteArray())

        // When
        val result = storeInCache.invoke(cacheId, cacheKey, inputStream)

        // Then
        assertTrue(result)

        // Check that the cache-id and cache-key directories exist
        val cacheIdDir = tempDir.resolve(cacheId.id)
        val cacheKeyDir = cacheIdDir.resolve(cacheKey.id)
        assertTrue(Files.exists(cacheIdDir))
        assertTrue(Files.exists(cacheKeyDir))

        // Check that the file with SHA-256 hash name exists
        val hashFile = cacheKeyDir.resolve(expectedHash)
        assertTrue(Files.exists(hashFile))
        assertEquals(content, Files.readString(hashFile))

        // Check that the 'latest' symlink exists and points to the hash file
        val latestLink = cacheKeyDir.resolve("latest")
        assertTrue(Files.exists(latestLink))
        assertTrue(Files.isSymbolicLink(latestLink))
        assertEquals(hashFile.fileName, Files.readSymbolicLink(latestLink))
    }

    @Test
    fun `should retrieve cache entry successfully using latest symlink`() {
        // Given
        val cacheId = CacheId("test-cache")
        val cacheKey = CacheKey("test-key")
        val content = "test content"

        // Calculate SHA-256 hash
        val hash = calculateSha256(content.toByteArray())

        // Create directory structure
        val cacheIdDir = tempDir.resolve(cacheId.id)
        val cacheKeyDir = cacheIdDir.resolve(cacheKey.id)
        Files.createDirectories(cacheKeyDir)

        // Create hash file
        val hashFile = cacheKeyDir.resolve(hash)
        Files.writeString(hashFile, content)

        // Create 'latest' symlink
        val latestLink = cacheKeyDir.resolve("latest")
        Files.createSymbolicLink(latestLink, hashFile.fileName)

        // When
        val result = retrieveFromCache.invoke(cacheId, cacheKey)

        // Then
        val resultContent = result.readAllBytes().toString(Charsets.UTF_8)
        assertEquals(content, resultContent)
    }

    @Test
    fun `should throw FileNotFoundException when cache entry does not exist`() {
        // Given
        val cacheId = CacheId("non-existent-cache")
        val cacheKey = CacheKey("non-existent-key")

        // When/Then
        assertThrows(FileNotFoundException::class.java) {
            retrieveFromCache.invoke(cacheId, cacheKey)
        }
    }

    @Test
    fun `should throw FileNotFoundException when latest symlink does not exist`() {
        // Given
        val cacheId = CacheId("test-cache")
        val cacheKey = CacheKey("test-key")

        // Create directory structure without 'latest' symlink
        val cacheIdDir = tempDir.resolve(cacheId.id)
        val cacheKeyDir = cacheIdDir.resolve(cacheKey.id)
        Files.createDirectories(cacheKeyDir)

        // When/Then
        assertThrows(FileNotFoundException::class.java) {
            retrieveFromCache.invoke(cacheId, cacheKey)
        }
    }

    @Test
    fun `should store and update latest symlink when storing multiple versions`() {
        // Given
        val cacheId = CacheId("test-cache")
        val cacheKey = CacheKey("test-key")
        val content1 = "test content 1"
        val content2 = "test content 2"

        // Calculate expected hashes
        val hash1 = calculateSha256(content1.toByteArray())
        val hash2 = calculateSha256(content2.toByteArray())

        // When - Store first version
        val result1 =
            storeInCache.invoke(cacheId, cacheKey, ByteArrayInputStream(content1.toByteArray()))

        // Then - Check first version
        assertTrue(result1)
        val cacheKeyDir = tempDir.resolve(cacheId.id).resolve(cacheKey.id)
        val hashFile1 = cacheKeyDir.resolve(hash1)
        val latestLink = cacheKeyDir.resolve("latest")
        assertTrue(Files.exists(hashFile1))
        assertTrue(Files.exists(latestLink))
        assertEquals(hashFile1.fileName, Files.readSymbolicLink(latestLink))

        // When - Store second version
        val result2 =
            storeInCache.invoke(cacheId, cacheKey, ByteArrayInputStream(content2.toByteArray()))

        // Then - Check second version
        assertTrue(result2)
        val hashFile2 = cacheKeyDir.resolve(hash2)
        assertTrue(Files.exists(hashFile2))
        assertTrue(Files.exists(latestLink))
        assertEquals(hashFile2.fileName, Files.readSymbolicLink(latestLink))

        // Both files should exist
        assertTrue(Files.exists(hashFile1))
        assertTrue(Files.exists(hashFile2))
    }

    @Test
    fun `should not create duplicate files when storing same content multiple times`() {
        // Given
        val cacheId = CacheId("test-cache")
        val cacheKey = CacheKey("test-key")
        val content = "test content"
        val hash = calculateSha256(content.toByteArray())

        // When - Store content twice
        val result1 =
            storeInCache.invoke(cacheId, cacheKey, ByteArrayInputStream(content.toByteArray()))
        val result2 =
            storeInCache.invoke(cacheId, cacheKey, ByteArrayInputStream(content.toByteArray()))

        // Then
        assertTrue(result1)
        assertTrue(result2)

        // Check that only one file exists with the hash name
        val cacheKeyDir = tempDir.resolve(cacheId.id).resolve(cacheKey.id)
        val hashFile = cacheKeyDir.resolve(hash)
        assertTrue(Files.exists(hashFile))

        // Check that no temp files remain
        val tempFiles =
            Files.list(cacheKeyDir).filter { it.fileName.toString().startsWith("temp-") }
        assertEquals(0, tempFiles.count())
    }

    @Test
    fun `should serialize and deserialize correctly when using CacheId`() {
        val om = jacksonObjectMapper()
        val id: CacheId = om.readValue("\"test-123\"")
        assertEquals(CacheId("test-123"), id)
        assertEquals("\"test-123\"", om.writeValueAsString(id))
    }

    // Helper method to calculate SHA-256 hash
    private fun calculateSha256(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(bytes)
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
