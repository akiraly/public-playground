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

class CacheServiceTest {

    @TempDir
    lateinit var tempDir: Path

    private lateinit var storeInCache: StoreInCacheWithCacheDir
    private lateinit var retrieveFromCache: RetrieveFromCacheWithCacheDir
    private lateinit var cacheDirectory: CacheDirectory

    @BeforeEach
    fun setUp() {
        cacheDirectory = CacheDirectory(tempDir.toString())
        storeInCache = StoreInCacheWithCacheDir(cacheDirectory)
        retrieveFromCache = RetrieveFromCacheWithCacheDir(cacheDirectory)
    }

    @Test
    fun `should store cache entry successfully`() {
        // Given
        val cacheId = CacheId("test-cache")
        val cacheKey = CacheKey("test-key")
        val content = "test content"
        val inputStream = ByteArrayInputStream(content.toByteArray())

        // When
        val result = storeInCache.invoke(cacheId, cacheKey, inputStream)

        // Then
        assertTrue(result)
        val cacheFile = tempDir.resolve(cacheId.toString()).resolve(cacheKey.toString())
        assertTrue(Files.exists(cacheFile))
        assertEquals(content, Files.readString(cacheFile))
    }

    @Test
    fun `should retrieve cache entry successfully`() {
        // Given
        val cacheId = CacheId("test-cache")
        val cacheKey = CacheKey("test-key")
        val content = "test content"
        val cacheIdDir = tempDir.resolve(cacheId.toString())
        Files.createDirectories(cacheIdDir)
        val cacheFile = cacheIdDir.resolve(cacheKey.toString())
        Files.writeString(cacheFile, content)

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
    fun `should serialize and deserialize correctly when using CacheId`() {
        val om = jacksonObjectMapper()
        val id: CacheId = om.readValue("\"test-123\"")
        assertEquals(CacheId("test-123"), id)
        assertEquals("\"test-123\"", om.writeValueAsString(id))
    }
}
