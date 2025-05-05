package io.github.akiraly.sghbc.cache

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpStatus
import java.io.FileNotFoundException
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class HttpGetCacheEntryTest {

    private lateinit var retrieveFromCache: RetrieveFromCache
    private lateinit var httpGetCacheEntry: HttpGetCacheEntry
    private lateinit var cacheId: CacheId
    private lateinit var cacheKey: CacheKey

    @BeforeEach
    fun setup() {
        retrieveFromCache = mockk()
        httpGetCacheEntry = HttpGetCacheEntry(retrieveFromCache)
        cacheId = CacheId("1234567890abcdef1234567890abcdef")
        cacheKey = CacheKey("abcdef1234567890abcdef1234567890")
    }

    @Test
    fun `should return 200 OK with cache entry when it exists`() {
        // Given
        val content = "Test cache content"
        val resource = ByteArrayResource(content.toByteArray())
        every { retrieveFromCache.invoke(cacheId, cacheKey) } returns resource

        // When
        val response = httpGetCacheEntry(cacheId, cacheKey)

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        verify { retrieveFromCache.invoke(cacheId, cacheKey) }
    }

    @Test
    fun `should return 404 Not Found when cache entry doesn't exist`() {
        // Given
        every { retrieveFromCache.invoke(cacheId, cacheKey) } throws FileNotFoundException("Not found")

        // When
        val response = httpGetCacheEntry(cacheId, cacheKey)

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        verify { retrieveFromCache.invoke(cacheId, cacheKey) }
    }

    @Test
    fun `should return 500 Internal Server Error when an unexpected error occurs`() {
        // Given
        every { retrieveFromCache.invoke(cacheId, cacheKey) } throws RuntimeException("Unexpected error")

        // When
        val response = httpGetCacheEntry(cacheId, cacheKey)

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        verify { retrieveFromCache.invoke(cacheId, cacheKey) }
    }
}
