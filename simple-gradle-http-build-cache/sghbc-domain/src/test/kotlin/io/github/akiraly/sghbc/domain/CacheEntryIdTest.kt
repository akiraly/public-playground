package io.github.akiraly.sghbc.domain

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class CacheEntryIdTest {

    @Test
    fun `should create valid CacheEntryId`() {
        // Given
        val cacheId = CacheId("test-cache")
        val gradleCacheKey = GradleCacheKey("1234abcdef1234abcdef1234abcdef1234abcdef")

        // When
        val cacheEntryId = CacheEntryId(cacheId, gradleCacheKey)

        // Then
        assertEquals(cacheId, cacheEntryId.cacheId)
        assertEquals(gradleCacheKey, cacheEntryId.gradleCacheKey)
    }

    @Test
    fun `should implement equals and hashCode correctly`() {
        // Given
        val cacheId1 = CacheId("test-cache")
        val gradleCacheKey1 = GradleCacheKey("1234abcdef1234abcdef1234abcdef1234abcdef")
        val cacheEntryId1 = CacheEntryId(cacheId1, gradleCacheKey1)

        val cacheId2 = CacheId("test-cache")
        val gradleCacheKey2 = GradleCacheKey("1234abcdef1234abcdef1234abcdef1234abcdef")
        val cacheEntryId2 = CacheEntryId(cacheId2, gradleCacheKey2)

        val cacheId3 = CacheId("different-cache")
        val gradleCacheKey3 = GradleCacheKey("1234abcdef1234abcdef1234abcdef1234abcdef")
        val cacheEntryId3 = CacheEntryId(cacheId3, gradleCacheKey3)

        val cacheId4 = CacheId("test-cache")
        val gradleCacheKey4 = GradleCacheKey("5678abcdef5678abcdef5678abcdef5678abcdef")
        val cacheEntryId4 = CacheEntryId(cacheId4, gradleCacheKey4)

        // Then
        assertEquals(cacheEntryId1, cacheEntryId2)
        assertEquals(cacheEntryId1.hashCode(), cacheEntryId2.hashCode())

        assertNotEquals(cacheEntryId1, cacheEntryId3)
        assertNotEquals(cacheEntryId1, cacheEntryId4)
    }

    @Test
    fun `should have correct string representation`() {
        // Given
        val cacheId = CacheId("test-cache")
        val gradleCacheKey = GradleCacheKey("1234abcdef1234abcdef1234abcdef1234abcdef")
        val cacheEntryId = CacheEntryId(cacheId, gradleCacheKey)

        // When
        val stringRepresentation = cacheEntryId.toString()

        // Then
        assert(stringRepresentation.contains("test-cache"))
        assert(stringRepresentation.contains("1234abcdef1234abcdef1234abcdef1234abcdef"))
    }
}
