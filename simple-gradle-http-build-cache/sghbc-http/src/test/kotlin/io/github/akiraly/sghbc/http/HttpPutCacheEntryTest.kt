package io.github.akiraly.sghbc.http

import io.github.akiraly.sghbc.domain.CacheEntryId
import io.github.akiraly.sghbc.domain.CacheId
import io.github.akiraly.sghbc.domain.GradleCacheKey
import io.github.akiraly.sghbc.domain.StoreInCache
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.io.IOException

class HttpPutCacheEntryTest {

    @Test
    fun `should return 200 OK when cache entry is stored successfully`() {
        // Given
        val cacheId = CacheId("test-cache")
        val gradleCacheKey = GradleCacheKey("1234abcdef1234abcdef1234abcdef1234abcdef")
        val cacheEntryId = CacheEntryId(cacheId, gradleCacheKey)
        val cacheContent = "test cache content".toByteArray()

        val storeInCache = mockk<StoreInCache>()
        every { storeInCache.invoke(any(), any()) } returns true

        val controller = HttpPutCacheEntry(storeInCache, 100)
        val mockMvc = MockMvcBuilders.standaloneSetup(controller).build()

        // When/Then
        mockMvc.perform(
            put(
                "/cache/{cacheId}/{gradleCacheKey}",
                "test-cache",
                "1234abcdef1234abcdef1234abcdef1234abcdef"
            )
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .content(cacheContent)
        )
            .andExpect(status().isOk)

        verify { storeInCache.invoke(cacheEntryId, any()) }
    }

    @Test
    fun `should return 500 Internal Server Error when storing cache entry fails`() {
        // Given
        val cacheId = CacheId("test-cache")
        val gradleCacheKey = GradleCacheKey("1234abcdef1234abcdef1234abcdef1234abcdef")
        val cacheEntryId = CacheEntryId(cacheId, gradleCacheKey)
        val cacheContent = "test cache content".toByteArray()

        val storeInCache = mockk<StoreInCache>()
        every { storeInCache.invoke(any(), any()) } returns false

        val controller = HttpPutCacheEntry(storeInCache, 100)
        val mockMvc = MockMvcBuilders.standaloneSetup(controller).build()

        // When/Then
        mockMvc.perform(
            put(
                "/cache/{cacheId}/{gradleCacheKey}",
                "test-cache",
                "1234abcdef1234abcdef1234abcdef1234abcdef"
            )
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .content(cacheContent)
        )
            .andExpect(status().isInternalServerError)

        verify { storeInCache.invoke(cacheEntryId, any()) }
    }

    @Test
    fun `should return 413 Payload Too Large when content length exceeds maximum allowed size`() {
        // Given
        val cacheContent = "test cache content".toByteArray()
        // Set maxSizeInMb to a very small value to ensure content length exceeds it
        val maxSizeInMb = 1L // 1 MB

        val storeInCache = mockk<StoreInCache>()
        // We don't need to configure storeInCache behavior because the controller should
        // return 413 before calling storeInCache when content length exceeds max size

        val controller = HttpPutCacheEntry(storeInCache, maxSizeInMb)
        val mockMvc = MockMvcBuilders.standaloneSetup(controller).build()

        // When/Then
        mockMvc.perform(
            put(
                "/cache/{cacheId}/{gradleCacheKey}",
                "test-cache",
                "1234abcdef1234abcdef1234abcdef1234abcdef"
            )
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .content(cacheContent)
                .header(
                    HttpHeaders.CONTENT_LENGTH,
                    1024 * 1024 * 2
                ) // Set content length to 2MB to exceed the max size
        )
            .andExpect(status().isPayloadTooLarge)
    }

    @Test
    fun `should return 413 Payload Too Large when IOException with 'too large' message occurs`() {
        // Given
        val cacheId = CacheId("test-cache")
        val gradleCacheKey = GradleCacheKey("1234abcdef1234abcdef1234abcdef1234abcdef")
        val cacheEntryId = CacheEntryId(cacheId, gradleCacheKey)
        val cacheContent = "test cache content".toByteArray()

        val storeInCache = mockk<StoreInCache>()
        every { storeInCache.invoke(any(), any()) } throws IOException("Payload too large")

        val controller = HttpPutCacheEntry(storeInCache, 100)
        val mockMvc = MockMvcBuilders.standaloneSetup(controller).build()

        // When/Then
        mockMvc.perform(
            put(
                "/cache/{cacheId}/{gradleCacheKey}",
                "test-cache",
                "1234abcdef1234abcdef1234abcdef1234abcdef"
            )
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .content(cacheContent)
        )
            .andExpect(status().isPayloadTooLarge)

        verify { storeInCache.invoke(cacheEntryId, any()) }
    }

    @Test
    fun `should return 500 Internal Server Error when an unexpected error occurs`() {
        // Given
        val cacheId = CacheId("test-cache")
        val gradleCacheKey = GradleCacheKey("1234abcdef1234abcdef1234abcdef1234abcdef")
        val cacheEntryId = CacheEntryId(cacheId, gradleCacheKey)
        val cacheContent = "test cache content".toByteArray()

        val storeInCache = mockk<StoreInCache>()
        every { storeInCache.invoke(any(), any()) } throws RuntimeException("Unexpected error")

        val controller = HttpPutCacheEntry(storeInCache, 100)
        val mockMvc = MockMvcBuilders.standaloneSetup(controller).build()

        // When/Then
        mockMvc.perform(
            put(
                "/cache/{cacheId}/{gradleCacheKey}",
                "test-cache",
                "1234abcdef1234abcdef1234abcdef1234abcdef"
            )
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .content(cacheContent)
        )
            .andExpect(status().isInternalServerError)

        verify { storeInCache.invoke(cacheEntryId, any()) }
    }

    @Test
    fun `should return 500 Internal Server Error when IOException without 'too large' message occurs`() {
        // Given
        val cacheId = CacheId("test-cache")
        val gradleCacheKey = GradleCacheKey("1234abcdef1234abcdef1234abcdef1234abcdef")
        val cacheEntryId = CacheEntryId(cacheId, gradleCacheKey)
        val cacheContent = "test cache content".toByteArray()

        val storeInCache = mockk<StoreInCache>()
        every { storeInCache.invoke(any(), any()) } throws IOException("Some other IO error")

        val controller = HttpPutCacheEntry(storeInCache, 100)
        val mockMvc = MockMvcBuilders.standaloneSetup(controller).build()

        // When/Then
        mockMvc.perform(
            put(
                "/cache/{cacheId}/{gradleCacheKey}",
                "test-cache",
                "1234abcdef1234abcdef1234abcdef1234abcdef"
            )
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .content(cacheContent)
        )
            .andExpect(status().isInternalServerError)

        verify { storeInCache.invoke(cacheEntryId, any()) }
    }
}
