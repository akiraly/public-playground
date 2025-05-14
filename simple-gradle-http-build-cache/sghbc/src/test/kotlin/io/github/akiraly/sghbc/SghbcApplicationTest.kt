package io.github.akiraly.sghbc

import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class SghbcApplicationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `should load application context`() {
        // Verifies that Spring context loads successfully
    }

    @Test
    fun `should have exactly two endpoints in OpenAPI documentation`() {
        mockMvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.paths.*.*").value<Collection<Any>>(hasSize(2)))
            .andExpect(jsonPath("$.paths.['/cache/{cacheId}/{gradleCacheKey}'].get").exists())
            .andExpect(jsonPath("$.paths.['/cache/{cacheId}/{gradleCacheKey}'].put").exists())
            .let { println(it.andReturn().response.contentAsString) }
    }
}
