package io.github.akiraly.contactsapp

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(GetContacts::class)
class GetContactTests {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `GetContacts should return hello world`() {
        mockMvc.perform(get("/contacts"))
            .andExpect(status().isOk)
            .andExpect(content().string("Contacts: Hello, World!"))
    }
}
