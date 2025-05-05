package io.github.akiraly.sghbc.cache

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class CacheIdAndKeyTest {

    @Test
    fun `should create valid CacheId`() {
        // Given
        val validId = "1234567890abcdef1234567890abcdef"

        // When
        val cacheId = CacheId(validId)

        // Then
        assertEquals(validId, cacheId.id)
    }

    @Test
    fun `should throw exception for CacheId with invalid characters`() {
        // Given
        val invalidId = "1234567890abcdefg" // contains 'g' which is not valid hex

        // When/Then
        val exception = assertThrows<IllegalArgumentException> {
            CacheId(invalidId)
        }

        // Then
        assert(exception.message?.contains("Invalid ID") == true)
    }

    @Test
    fun `should throw exception for CacheId with invalid length`() {
        // Given
        val tooShortId = "1234567890abcdef" // less than 32 characters

        // When/Then
        val exception = assertThrows<IllegalArgumentException> {
            CacheId(tooShortId)
        }

        // Then
        assert(exception.message?.contains("Invalid ID") == true)
    }

    @Test
    fun `should throw exception for CacheId with too long length`() {
        // Given
        val tooLongId = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef1" // 65 characters

        // When/Then
        val exception = assertThrows<IllegalArgumentException> {
            CacheId(tooLongId)
        }

        // Then
        assert(exception.message?.contains("Invalid ID with length") == true)
    }

    @Test
    fun `should create valid CacheKey`() {
        // Given
        val validId = "1234567890abcdef1234567890abcdef"

        // When
        val cacheKey = CacheKey(validId)

        // Then
        assertEquals(validId, cacheKey.id)
    }

    @Test
    fun `should throw exception for CacheKey with invalid characters`() {
        // Given
        val invalidId = "1234567890ABCDEF" // contains uppercase letters which are not valid

        // When/Then
        val exception = assertThrows<IllegalArgumentException> {
            CacheKey(invalidId)
        }

        // Then
        assert(exception.message?.contains("Invalid ID") == true)
    }

    @Test
    fun `should throw exception for CacheKey with invalid length`() {
        // Given
        val tooShortId = "1234567890abcdef" // less than 32 characters

        // When/Then
        val exception = assertThrows<IllegalArgumentException> {
            CacheKey(tooShortId)
        }

        // Then
        assert(exception.message?.contains("Invalid ID") == true)
    }

    @Test
    fun `should throw exception for CacheKey with too long length`() {
        // Given
        val tooLongId = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef1" // 65 characters

        // When/Then
        val exception = assertThrows<IllegalArgumentException> {
            CacheKey(tooLongId)
        }

        // Then
        assert(exception.message?.contains("Invalid ID with length") == true)
    }
}
