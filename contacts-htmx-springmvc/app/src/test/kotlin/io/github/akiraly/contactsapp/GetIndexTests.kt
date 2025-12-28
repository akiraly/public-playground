package io.github.akiraly.contactsapp

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(GetIndex::class)
class GetIndexTests {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `GetIndex should redirect to contacts`() {
        mockMvc.perform(get("/"))
            .andExpect(status().isTemporaryRedirect)
            .andExpect(redirectedUrl("/contacts"))
    }
}
