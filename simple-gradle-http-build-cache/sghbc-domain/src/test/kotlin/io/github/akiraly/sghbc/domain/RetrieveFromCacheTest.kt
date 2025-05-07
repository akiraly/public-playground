package io.github.akiraly.sghbc.domain

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.core.io.Resource
import java.io.FileNotFoundException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RetrieveFromCacheTest {

    @Test
    fun `should retrieve cache entry when it exists`() {
        // Given
        val cacheId = CacheId("test-cache")
        val gradleCacheKey = GradleCacheKey("1234abcdef1234abcdef1234abcdef1234abcdef")
        val cacheEntryId = CacheEntryId(cacheId, gradleCacheKey)
        val resource = mockk<Resource>()
        val expectedCacheEntry = CacheEntry(cacheEntryId, resource)

        val retrieveFromCache: RetrieveFromCache = mockk()
        every { retrieveFromCache.invoke(cacheEntryId) } returns expectedCacheEntry

        // When
        val result = retrieveFromCache(cacheEntryId)

        // Then
        assertEquals(expectedCacheEntry, result)
        verify { retrieveFromCache.invoke(cacheEntryId) }
    }

    @Test
    fun `should throw FileNotFoundException when cache entry does not exist`() {
        // Given
        val cacheId = CacheId("test-cache")
        val gradleCacheKey = GradleCacheKey("1234abcdef1234abcdef1234abcdef1234abcdef")
        val cacheEntryId = CacheEntryId(cacheId, gradleCacheKey)

        val retrieveFromCache: RetrieveFromCache = mockk()
        every { retrieveFromCache.invoke(cacheEntryId) } throws FileNotFoundException("Cache entry not found")

        // When/Then
        assertFailsWith<FileNotFoundException> {
            retrieveFromCache(cacheEntryId)
        }
        verify { retrieveFromCache.invoke(cacheEntryId) }
    }

    @Test
    fun `should implement RetrieveFromCache interface correctly`() {
        // Given
        val cacheId = CacheId("test-cache")
        val gradleCacheKey = GradleCacheKey("1234abcdef1234abcdef1234abcdef1234abcdef")
        val cacheEntryId = CacheEntryId(cacheId, gradleCacheKey)
        val resource = mockk<Resource>()
        val expectedCacheEntry = CacheEntry(cacheEntryId, resource)

        // Create a concrete implementation of RetrieveFromCache
        val retrieveFromCache = RetrieveFromCache { id ->
            if (id == cacheEntryId) {
                expectedCacheEntry
            } else {
                throw FileNotFoundException("Cache entry not found")
            }
        }

        // When
        val result = retrieveFromCache(cacheEntryId)

        // Then
        assertEquals(expectedCacheEntry, result)

        // When/Then - Test with different ID
        val differentCacheId = CacheId("different-cache")
        val differentGradleCacheKey = GradleCacheKey("5678abcdef5678abcdef5678abcdef5678abcdef")
        val differentCacheEntryId = CacheEntryId(differentCacheId, differentGradleCacheKey)

        assertFailsWith<FileNotFoundException> {
            retrieveFromCache(differentCacheEntryId)
        }
    }
}
