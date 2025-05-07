package io.github.akiraly.sghbc.http

import io.github.akiraly.sghbc.domain.CacheEntry
import io.github.akiraly.sghbc.domain.CacheEntryId
import io.github.akiraly.sghbc.domain.CacheId
import io.github.akiraly.sghbc.domain.GradleCacheKey
import io.github.akiraly.sghbc.domain.RetrieveFromCache
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.io.FileNotFoundException

class HttpGetCacheEntryTest {

    @Test
    fun `should return 200 OK with cache entry when it exists`() {
        // Given
        val cacheId = CacheId("test-cache")
        val gradleCacheKey = GradleCacheKey("1234abcdef1234abcdef1234abcdef1234abcdef")
        val cacheEntryId = CacheEntryId(cacheId, gradleCacheKey)
        val cacheContent = "test cache content".toByteArray()
        val resource = ByteArrayResource(cacheContent)
        val cacheEntry = CacheEntry(cacheEntryId, resource)

        val retrieveFromCache = mockk<RetrieveFromCache>()
        every { retrieveFromCache.invoke(cacheEntryId) } returns cacheEntry

        val controller = HttpGetCacheEntry(retrieveFromCache)
        val mockMvc = MockMvcBuilders.standaloneSetup(controller).build()

        // When/Then
        mockMvc.perform(
            get(
                "/cache/{cacheId}/{gradleCacheKey}",
                "test-cache",
                "1234abcdef1234abcdef1234abcdef1234abcdef"
            )
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
            .andExpect(content().bytes(cacheContent))

        verify { retrieveFromCache.invoke(cacheEntryId) }
    }

    @Test
    fun `should return 404 Not Found when cache entry does not exist`() {
        // Given
        val cacheId = CacheId("test-cache")
        val gradleCacheKey = GradleCacheKey("1234abcdef1234abcdef1234abcdef1234abcdef")
        val cacheEntryId = CacheEntryId(cacheId, gradleCacheKey)

        val retrieveFromCache = mockk<RetrieveFromCache>()
        every { retrieveFromCache.invoke(cacheEntryId) } throws FileNotFoundException("Cache entry not found")

        val controller = HttpGetCacheEntry(retrieveFromCache)
        val mockMvc = MockMvcBuilders.standaloneSetup(controller).build()

        // When/Then
        mockMvc.perform(
            get(
                "/cache/{cacheId}/{gradleCacheKey}",
                "test-cache",
                "1234abcdef1234abcdef1234abcdef1234abcdef"
            )
        )
            .andExpect(status().isNotFound)

        verify { retrieveFromCache.invoke(cacheEntryId) }
    }

    @Test
    fun `should return 500 Internal Server Error when an unexpected error occurs`() {
        // Given
        val cacheId = CacheId("test-cache")
        val gradleCacheKey = GradleCacheKey("1234abcdef1234abcdef1234abcdef1234abcdef")
        val cacheEntryId = CacheEntryId(cacheId, gradleCacheKey)

        val retrieveFromCache = mockk<RetrieveFromCache>()
        every { retrieveFromCache.invoke(cacheEntryId) } throws RuntimeException("Unexpected error")

        val controller = HttpGetCacheEntry(retrieveFromCache)
        val mockMvc = MockMvcBuilders.standaloneSetup(controller).build()

        // When/Then
        mockMvc.perform(
            get(
                "/cache/{cacheId}/{gradleCacheKey}",
                "test-cache",
                "1234abcdef1234abcdef1234abcdef1234abcdef"
            )
        )
            .andExpect(status().isInternalServerError)

        verify { retrieveFromCache.invoke(cacheEntryId) }
    }
}
