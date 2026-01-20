package io.github.akiraly.contactsapp.web

import io.github.akiraly.contactsapp.domain.Contact
import io.github.akiraly.contactsapp.domain.ContactId
import io.github.akiraly.contactsapp.repo.LoadAllContacts
import io.github.akiraly.contactsapp.repo.SearchContacts
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(GetContactListPage::class)
class GetContactTests {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var loadAllContacts: LoadAllContacts

    @MockitoBean
    private lateinit var searchContacts: SearchContacts

    @Test
    fun `GetContacts should return contact json`() {

        whenever(loadAllContacts()).thenReturn(
            setOf(
                Contact(
                    ContactId("1"),
                    "John",
                    "Doe",
                    "+36301234567",
                    "john.doe@example.com"
                ),
                Contact(
                    ContactId("2"),
                    "Jane",
                    "Doe",
                    "+36301234568",
                    "jane.doe@example.com"
                )
            )
        )

        mockMvc.perform(get("/contacts"))
            .andExpect(status().isOk)
            .andExpect(content().string("""[{"id":"1","first":"John","last":"Doe","phone":"+36301234567","email":"john.doe@example.com"},{"id":"2","first":"Jane","last":"Doe","phone":"+36301234568","email":"jane.doe@example.com"}]"""))
    }
}
