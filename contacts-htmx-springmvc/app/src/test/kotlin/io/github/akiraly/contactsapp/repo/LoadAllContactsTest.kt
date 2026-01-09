package io.github.akiraly.contactsapp.repo

import io.github.akiraly.contactsapp.domain.ContactId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.nio.file.Path

class LoadAllContactsTest {

    @Test
    fun `LoadAllContacts should load contacts from file`() {
        val db = ContactDb(Path.of("contacts.db.json"))
        val loadAllContacts = LoadAllContacts(db)
        val contacts = loadAllContacts()

        assertEquals(100, contacts.size)
        val first = contacts.single { it.id == ContactId("1") }
        assertEquals("Mary", first.first)
        assertEquals("Smith", first.last)
    }

}
