package io.github.akiraly.contactsapp.domain

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ContactTest {
    @Test
    fun `When the matches() is called with a parameter that is contained by the first, last, email or phone then the method should return true `() {
        val contact = Contact(ContactId("1"), "John", "Doe", "john.doe@example.com", "123-456-7890")
        assertTrue(contact.matches("john"))
        assertTrue(contact.matches("doe"))
        assertTrue(contact.matches("john.doe@example.com"))
        assertTrue(contact.matches("123-456-7890"))
    }
}
