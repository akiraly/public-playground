package io.github.akiraly.sghbc.cache

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CacheIntegrationTest {

    @TempDir
    lateinit var tempDir: Path

    private lateinit var cacheDirectory: CacheDirectory
    private lateinit var retrieveFromCache: RetrieveFromCache
    private lateinit var storeInCache: StoreInCache
    private lateinit var httpGetCacheEntry: HttpGetCacheEntry
    private lateinit var httpPutCacheEntry: HttpPutCacheEntry
    private lateinit var cacheId: CacheId
    private lateinit var cacheKey: CacheKey

    @BeforeEach
    fun setup() {
        cacheDirectory = CacheDirectory(tempDir.toString())
        retrieveFromCache = RetrieveFromCacheDir(cacheDirectory)
        storeInCache = StoreInCacheDir(cacheDirectory, 100)
        httpGetCacheEntry = HttpGetCacheEntry(retrieveFromCache)
        httpPutCacheEntry = HttpPutCacheEntry(storeInCache, 100)

        cacheId = CacheId("1234567890abcdef1234567890abcdef")
        cacheKey = CacheKey("abcdef1234567890abcdef1234567890")
    }

    @Test
    fun `should store and retrieve cache entry through HTTP controllers`() {
        // Given
        val content = "Test cache content for integration test"
        val resource = ByteArrayResource(content.toByteArray())
        val headers = HttpHeaders()
        headers.contentLength = content.length.toLong()

        // When - Store the cache entry
        val putResponse = httpPutCacheEntry(cacheId, cacheKey, headers, resource)

        // Then - Verify the PUT response
        assertEquals(HttpStatus.OK, putResponse.statusCode, "PUT request should succeed")

        // When - Retrieve the cache entry
        val getResponse = httpGetCacheEntry(cacheId, cacheKey)

        // Then - Verify the GET response
        assertEquals(HttpStatus.OK, getResponse.statusCode, "GET request should succeed")
        assertNotNull(getResponse.body, "Response body should not be null")

        // Verify the content matches
        val retrievedContent = getResponse.body!!.inputStream.bufferedReader().use { it.readText() }
        assertEquals(content, retrievedContent, "Retrieved content should match original content")

        // Verify the file system structure
        val cacheKeyDir = cacheDirectory.resolveDir(cacheId, cacheKey)
        assertTrue(Files.exists(cacheKeyDir), "Cache key directory should exist")

        val latestLink = cacheKeyDir.resolve("latest")
        assertTrue(Files.exists(latestLink), "Latest symlink should exist")
        assertTrue(Files.isSymbolicLink(latestLink), "Latest should be a symlink")

        val targetFile = cacheKeyDir.resolve(Files.readSymbolicLink(latestLink))
        assertTrue(Files.exists(targetFile), "Target file should exist")
        assertEquals(content, Files.readString(targetFile), "File content should match original content")
    }

    @Test
    fun `should return 404 when retrieving non-existent cache entry`() {
        // Given
        val nonExistentCacheId = CacheId("aaaabbbbccccddddeeeeffffaaaabbbb")

        // When
        val response = httpGetCacheEntry(nonExistentCacheId, cacheKey)

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode, "Should return 404 for non-existent cache entry")
    }

    @Test
    fun `should handle multiple stores and retrieves with same cache id and key`() {
        // Given
        val content1 = "First cache content"
        val content2 = "Updated cache content"
        val resource1 = ByteArrayResource(content1.toByteArray())
        val resource2 = ByteArrayResource(content2.toByteArray())
        val headers = HttpHeaders()

        // When - Store first content
        headers.contentLength = content1.length.toLong()
        httpPutCacheEntry(cacheId, cacheKey, headers, resource1)

        // Then - Verify first content can be retrieved
        val getResponse1 = httpGetCacheEntry(cacheId, cacheKey)
        assertEquals(HttpStatus.OK, getResponse1.statusCode)
        val retrievedContent1 = getResponse1.body!!.inputStream.bufferedReader().use { it.readText() }
        assertEquals(content1, retrievedContent1)

        // When - Store second content
        headers.contentLength = content2.length.toLong()
        httpPutCacheEntry(cacheId, cacheKey, headers, resource2)

        // Then - Verify second content can be retrieved
        val getResponse2 = httpGetCacheEntry(cacheId, cacheKey)
        assertEquals(HttpStatus.OK, getResponse2.statusCode)
        val retrievedContent2 = getResponse2.body!!.inputStream.bufferedReader().use { it.readText() }
        assertEquals(content2, retrievedContent2)

        // Verify both files exist in the cache directory
        val cacheKeyDir = cacheDirectory.resolveDir(cacheId, cacheKey)
        val fileCount = Files.list(cacheKeyDir).count()
        assertTrue(fileCount > 2, "Should have at least 2 files (content files + symlink)")
    }

    @Test
    fun `should handle empty content`() {
        // Given
        val emptyContent = ByteArray(0)
        val resource = ByteArrayResource(emptyContent)
        val headers = HttpHeaders()
        headers.contentLength = 0

        // When - Store empty content
        val putResponse = httpPutCacheEntry(cacheId, cacheKey, headers, resource)

        // Then - Verify the PUT response
        assertEquals(HttpStatus.OK, putResponse.statusCode, "PUT request should succeed with empty content")

        // When - Retrieve the empty cache entry
        val getResponse = httpGetCacheEntry(cacheId, cacheKey)

        // Then - Verify the GET response
        assertEquals(HttpStatus.OK, getResponse.statusCode, "GET request should succeed")
        assertNotNull(getResponse.body, "Response body should not be null")

        // Verify the content is empty
        val retrievedContent = getResponse.body!!.inputStream.bufferedReader().use { it.readText() }
        assertEquals("", retrievedContent, "Retrieved content should be empty")
    }
}
