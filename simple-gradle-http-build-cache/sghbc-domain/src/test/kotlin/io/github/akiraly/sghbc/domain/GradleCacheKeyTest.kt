package io.github.akiraly.sghbc.domain

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class GradleCacheKeyTest {

    @Test
    fun `should create valid GradleCacheKey`() {
        // Given
        val validKey = "1234abcdef1234abcdef1234abcdef1234abcdef" // Valid hexadecimal string with 40 characters

        // When
        val cacheKey = GradleCacheKey(validKey)

        // Then
        assertEquals(validKey, cacheKey.value)
    }

    @Test
    fun `should throw exception for GradleCacheKey with invalid characters`() {
        // Given
        val invalidKey = "1234abcdefg1234abcdef1234abcdef1234abcde" // 'g' is not a valid hex character

        // When/Then
        assertThrows<IllegalArgumentException> {
            GradleCacheKey(invalidKey)
        }
    }

    @Test
    fun `should throw exception for GradleCacheKey with less than 40 characters`() {
        // Given
        val invalidKey = "a".repeat(39) // 39 characters

        // When/Then
        assertThrows<IllegalArgumentException> {
            GradleCacheKey(invalidKey)
        }
    }

    @Test
    fun `should throw exception for GradleCacheKey with more than 40 characters`() {
        // Given
        val invalidKey = "a".repeat(41) // 41 characters

        // When/Then
        assertThrows<IllegalArgumentException> {
            GradleCacheKey(invalidKey)
        }
    }

    @Test
    fun `should implement equals and hashCode correctly`() {
        // Given
        val key1 = GradleCacheKey("1234abcdef1234abcdef1234abcdef1234abcdef")
        val key2 = GradleCacheKey("1234abcdef1234abcdef1234abcdef1234abcdef")
        val key3 = GradleCacheKey("5678abcdef5678abcdef5678abcdef5678abcdef")

        // Then
        assertEquals(key1, key2)
        assertEquals(key1.hashCode(), key2.hashCode())
        assert(key1 != key3)
    }
}
