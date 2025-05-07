package io.github.akiraly.sghbc.domain

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class CacheIdTest {

    @Test
    fun `should create valid CacheId`() {
        // Given
        val validId = "valid-cache-id"

        // When
        val cacheId = CacheId(validId)

        // Then
        assertEquals(validId, cacheId.value)
    }

    @Test
    fun `should create valid CacheId with minimum length`() {
        // Given
        val validId = "12345678" // 8 characters

        // When
        val cacheId = CacheId(validId)

        // Then
        assertEquals(validId, cacheId.value)
    }

    @Test
    fun `should create valid CacheId with maximum length`() {
        // Given
        val validId = "a".repeat(64) // 64 characters

        // When
        val cacheId = CacheId(validId)

        // Then
        assertEquals(validId, cacheId.value)
    }

    @Test
    fun `should throw exception for CacheId with invalid characters`() {
        // Given
        val invalidId = "invalid@id"

        // When/Then
        assertThrows<IllegalArgumentException> {
            CacheId(invalidId)
        }
    }

    @Test
    fun `should throw exception for CacheId that is too short`() {
        // Given
        val invalidId = "1234567" // 7 characters

        // When/Then
        assertThrows<IllegalArgumentException> {
            CacheId(invalidId)
        }
    }

    @Test
    fun `should throw exception for CacheId that is too long`() {
        // Given
        val invalidId = "a".repeat(65) // 65 characters

        // When/Then
        assertThrows<IllegalArgumentException> {
            CacheId(invalidId)
        }
    }

    @Test
    fun `should implement equals and hashCode correctly`() {
        // Given
        val id1 = CacheId("test-cache-id")
        val id2 = CacheId("test-cache-id")
        val id3 = CacheId("different-id")

        // Then
        assertEquals(id1, id2)
        assertEquals(id1.hashCode(), id2.hashCode())
        assert(id1 != id3)
    }
}
