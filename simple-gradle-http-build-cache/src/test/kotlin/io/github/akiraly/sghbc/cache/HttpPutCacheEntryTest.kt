package io.github.akiraly.sghbc.cache

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import java.io.IOException
import kotlin.test.assertEquals

class HttpPutCacheEntryTest {

    private lateinit var storeInCache: StoreInCache
    private lateinit var httpPutCacheEntry: HttpPutCacheEntry
    private lateinit var cacheId: CacheId
    private lateinit var cacheKey: CacheKey
    private lateinit var headers: HttpHeaders
    private val maxAllowedSizeInMb = 100L

    @BeforeEach
    fun setup() {
        storeInCache = mockk()
        httpPutCacheEntry = HttpPutCacheEntry(storeInCache, maxAllowedSizeInMb)
        cacheId = CacheId("1234567890abcdef1234567890abcdef")
        cacheKey = CacheKey("abcdef1234567890abcdef1234567890")
        headers = HttpHeaders()
    }

    @Test
    fun `should return 200 OK when cache entry is stored successfully`() {
        // Given
        val content = "Test cache content"
        val resource = ByteArrayResource(content.toByteArray())
        headers.contentLength = content.length.toLong()

        every { storeInCache.invoke(cacheId, cacheKey, resource) } returns true

        // When
        val response = httpPutCacheEntry(cacheId, cacheKey, headers, resource)

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        verify { storeInCache.invoke(cacheId, cacheKey, resource) }
    }

    @Test
    fun `should return 413 Payload Too Large when content length exceeds limit`() {
        // Given
        val content = "Test cache content"
        val resource = ByteArrayResource(content.toByteArray())
        val maxSizeInBytes = maxAllowedSizeInMb * 1024 * 1024
        headers.contentLength = maxSizeInBytes + 1 // Exceed the limit by 1 byte

        // When
        val response = httpPutCacheEntry(cacheId, cacheKey, headers, resource)

        // Then
        assertEquals(HttpStatus.PAYLOAD_TOO_LARGE, response.statusCode)
        // Verify storeInCache was not called
        verify(exactly = 0) { storeInCache.invoke(any(), any(), any()) }
    }

    @Test
    fun `should return 413 Payload Too Large when IOException with 'too large' message occurs`() {
        // Given
        val content = "Test cache content"
        val resource = ByteArrayResource(content.toByteArray())
        headers.contentLength = content.length.toLong()

        every { storeInCache.invoke(cacheId, cacheKey, resource) } throws
            IOException("The payload is too large for processing")

        // When
        val response = httpPutCacheEntry(cacheId, cacheKey, headers, resource)

        // Then
        assertEquals(HttpStatus.PAYLOAD_TOO_LARGE, response.statusCode)
        verify { storeInCache.invoke(cacheId, cacheKey, resource) }
    }

    @Test
    fun `should return 500 Internal Server Error when store operation fails`() {
        // Given
        val content = "Test cache content"
        val resource = ByteArrayResource(content.toByteArray())
        headers.contentLength = content.length.toLong()

        every { storeInCache.invoke(cacheId, cacheKey, resource) } returns false

        // When
        val response = httpPutCacheEntry(cacheId, cacheKey, headers, resource)

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        verify { storeInCache.invoke(cacheId, cacheKey, resource) }
    }

    @Test
    fun `should return 500 Internal Server Error when an unexpected error occurs`() {
        // Given
        val content = "Test cache content"
        val resource = ByteArrayResource(content.toByteArray())
        headers.contentLength = content.length.toLong()

        every { storeInCache.invoke(cacheId, cacheKey, resource) } throws
            RuntimeException("Unexpected error")

        // When
        val response = httpPutCacheEntry(cacheId, cacheKey, headers, resource)

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        verify { storeInCache.invoke(cacheId, cacheKey, resource) }
    }

    @Test
    fun `should handle missing content length header`() {
        // Given
        val content = "Test cache content"
        val resource = ByteArrayResource(content.toByteArray())
        // No content length header set

        every { storeInCache.invoke(cacheId, cacheKey, resource) } returns true

        // When
        val response = httpPutCacheEntry(cacheId, cacheKey, headers, resource)

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        verify { storeInCache.invoke(cacheId, cacheKey, resource) }
    }
}
