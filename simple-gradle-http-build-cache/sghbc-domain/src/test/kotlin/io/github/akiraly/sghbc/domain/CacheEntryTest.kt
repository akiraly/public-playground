package io.github.akiraly.sghbc.domain

import org.junit.jupiter.api.Test
import io.mockk.mockk
import org.springframework.core.io.Resource
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class CacheEntryTest {

    @Test
    fun `should create valid CacheEntry`() {
        // Given
        val cacheId = CacheId("test-cache")
        val gradleCacheKey = GradleCacheKey("1234abcdef1234abcdef1234abcdef1234abcdef")
        val cacheEntryId = CacheEntryId(cacheId, gradleCacheKey)
        val resource = mockk<Resource>()

        // When
        val cacheEntry = CacheEntry(cacheEntryId, resource)

        // Then
        assertEquals(cacheEntryId, cacheEntry.id)
        assertEquals(resource, cacheEntry.resource)
    }

    @Test
    fun `should implement equals and hashCode correctly`() {
        // Given
        val cacheId = CacheId("test-cache")
        val gradleCacheKey = GradleCacheKey("1234abcdef1234abcdef1234abcdef1234abcdef")
        val cacheEntryId = CacheEntryId(cacheId, gradleCacheKey)
        val resource1 = mockk<Resource>()
        val resource2 = mockk<Resource>()

        val cacheEntry1 = CacheEntry(cacheEntryId, resource1)
        val cacheEntry2 = CacheEntry(cacheEntryId, resource1)
        val cacheEntry3 = CacheEntry(cacheEntryId, resource2)

        val differentCacheId = CacheId("different-cache")
        val differentGradleCacheKey = GradleCacheKey("5678abcdef5678abcdef5678abcdef5678abcdef")
        val differentCacheEntryId = CacheEntryId(differentCacheId, differentGradleCacheKey)
        val cacheEntry4 = CacheEntry(differentCacheEntryId, resource1)

        // Then
        assertEquals(cacheEntry1, cacheEntry2)
        assertEquals(cacheEntry1.hashCode(), cacheEntry2.hashCode())

        // Note: In data classes, all properties are used for equals/hashCode
        // So different resources should result in different equality
        assertNotEquals(cacheEntry1, cacheEntry3)
        assertNotEquals(cacheEntry1, cacheEntry4)
    }

    @Test
    fun `should have correct string representation`() {
        // Given
        val cacheId = CacheId("test-cache")
        val gradleCacheKey = GradleCacheKey("1234abcdef1234abcdef1234abcdef1234abcdef")
        val cacheEntryId = CacheEntryId(cacheId, gradleCacheKey)
        val resource = mockk<Resource>()
        val cacheEntry = CacheEntry(cacheEntryId, resource)

        // When
        val stringRepresentation = cacheEntry.toString()

        // Then
        assert(stringRepresentation.contains("test-cache"))
        assert(stringRepresentation.contains("1234abcdef1234abcdef1234abcdef1234abcdef"))
    }
}
