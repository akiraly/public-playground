package io.github.akiraly.sghbc.cache

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.io.ByteArrayInputStream
import java.io.FileNotFoundException

class CacheControllerTest {

    private lateinit var mockMvc: MockMvc
    private val storeInCache: StoreInCache = mockk()
    private val retrieveFromCache: RetrieveFromCache = mockk()
    private lateinit var cacheController: CacheController

    @BeforeEach
    fun setUp() {
        cacheController = CacheController(storeInCache, retrieveFromCache)
        mockMvc = MockMvcBuilders.standaloneSetup(cacheController).build()
    }

    @Test
    fun `should return 200 when cache entry exists`() {
        // Given
        val cacheId = CacheId("test-cache")
        val cacheKey = CacheKey("test-key")
        val content = "test content"
        val inputStream = ByteArrayInputStream(content.toByteArray())

        every { retrieveFromCache.invoke(cacheId, cacheKey) } returns inputStream

        // When/Then
        mockMvc.perform(get("/cache/{cacheId}/{cacheKey}", cacheId.id, cacheKey.id))
            .andExpect(status().isOk)

        verify { retrieveFromCache.invoke(cacheId, cacheKey) }
    }

    @Test
    fun `should return 404 when cache entry does not exist`() {
        // Given
        val cacheId = CacheId("test-cache")
        val cacheKey = CacheKey("non-existent-key")

        every { retrieveFromCache.invoke(cacheId, cacheKey) } throws FileNotFoundException()

        // When/Then
        mockMvc.perform(get("/cache/{cacheId}/{cacheKey}", cacheId.id, cacheKey.id))
            .andExpect(status().isNotFound)

        verify { retrieveFromCache.invoke(cacheId, cacheKey) }
    }

    @Test
    fun `should return 200 when cache entry is stored successfully`() {
        // Given
        val cacheId = CacheId("test-cache")
        val cacheKey = CacheKey("test-key")
        val content = "test content"

        every { storeInCache.invoke(cacheId, cacheKey, any()) } returns true

        // When/Then
        mockMvc.perform(
            put("/cache/{cacheId}/{cacheKey}", cacheId.id, cacheKey.id)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .content(content)
        )
            .andExpect(status().isOk)

        verify { storeInCache.invoke(cacheId, cacheKey, any()) }
    }

    @Test
    fun `should return 500 when cache entry storage fails`() {
        // Given
        val cacheId = CacheId("test-cache")
        val cacheKey = CacheKey("test-key")
        val content = "test content"

        every { storeInCache.invoke(cacheId, cacheKey, any()) } returns false

        // When/Then
        mockMvc.perform(
            put("/cache/{cacheId}/{cacheKey}", cacheId.id, cacheKey.id)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .content(content)
        )
            .andExpect(status().isInternalServerError)

        verify { storeInCache.invoke(cacheId, cacheKey, any()) }
    }
}
