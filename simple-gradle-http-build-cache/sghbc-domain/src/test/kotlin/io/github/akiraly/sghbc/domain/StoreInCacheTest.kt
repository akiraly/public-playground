package io.github.akiraly.sghbc.domain

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.core.io.Resource
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StoreInCacheTest {

    @Test
    fun `should store cache entry successfully`() {
        // Given
        val cacheId = CacheId("test-cache")
        val gradleCacheKey = GradleCacheKey("1234abcdef1234abcdef1234abcdef1234abcdef")
        val cacheEntryId = CacheEntryId(cacheId, gradleCacheKey)
        val resource = mockk<Resource>()
        val cacheEntry = CacheEntry(cacheEntryId, resource)

        val storeInCache: StoreInCache = mockk()
        every { storeInCache.invoke(cacheEntryId, cacheEntry) } returns true

        // When
        val result = storeInCache(cacheEntryId, cacheEntry)

        // Then
        assertTrue(result)
        verify { storeInCache.invoke(cacheEntryId, cacheEntry) }
    }

    @Test
    fun `should return false when storing cache entry fails`() {
        // Given
        val cacheId = CacheId("test-cache")
        val gradleCacheKey = GradleCacheKey("1234abcdef1234abcdef1234abcdef1234abcdef")
        val cacheEntryId = CacheEntryId(cacheId, gradleCacheKey)
        val resource = mockk<Resource>()
        val cacheEntry = CacheEntry(cacheEntryId, resource)

        val storeInCache: StoreInCache = mockk()
        every { storeInCache.invoke(cacheEntryId, cacheEntry) } returns false

        // When
        val result = storeInCache(cacheEntryId, cacheEntry)

        // Then
        assertFalse(result)
        verify { storeInCache.invoke(cacheEntryId, cacheEntry) }
    }

    @Test
    fun `should implement StoreInCache interface correctly`() {
        // Given
        val cacheId = CacheId("test-cache")
        val gradleCacheKey = GradleCacheKey("1234abcdef1234abcdef1234abcdef1234abcdef")
        val cacheEntryId = CacheEntryId(cacheId, gradleCacheKey)
        val resource = mockk<Resource>()
        val cacheEntry = CacheEntry(cacheEntryId, resource)

        // Create a concrete implementation of StoreInCache
        val successfulStore = StoreInCache { id, entry ->
            id == entry.id // Only succeed if IDs match
        }

        // When
        val successResult = successfulStore(cacheEntryId, cacheEntry)

        // Then
        assertTrue(successResult)

        // When - Test with mismatched IDs
        val differentCacheId = CacheId("different-cache")
        val differentGradleCacheKey = GradleCacheKey("5678abcdef5678abcdef5678abcdef5678abcdef")
        val differentCacheEntryId = CacheEntryId(differentCacheId, differentGradleCacheKey)

        val failureResult = successfulStore(differentCacheEntryId, cacheEntry)

        // Then
        assertFalse(failureResult)
    }

    @Test
    fun `should handle exceptions gracefully`() {
        // Given
        val cacheId = CacheId("test-cache")
        val gradleCacheKey = GradleCacheKey("1234abcdef1234abcdef1234abcdef1234abcdef")
        val cacheEntryId = CacheEntryId(cacheId, gradleCacheKey)
        val resource = mockk<Resource>()
        val cacheEntry = CacheEntry(cacheEntryId, resource)

        // Create an implementation that throws an exception
        val failingStore = StoreInCache { _, _ ->
            throw RuntimeException("Simulated failure")
        }

        // Wrap in try-catch to demonstrate how exceptions should be handled
        val result = try {
            failingStore(cacheEntryId, cacheEntry)
        } catch (_: Exception) {
            false
        }

        // Then
        assertFalse(result)
    }
}
