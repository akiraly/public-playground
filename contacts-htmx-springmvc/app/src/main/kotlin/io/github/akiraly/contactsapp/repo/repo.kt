package io.github.akiraly.contactsapp.repo

import io.github.akiraly.contactsapp.domain.Contact
import io.github.akiraly.contactsapp.domain.ContactRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.readValue
import java.nio.file.Path

val logger: Logger = LoggerFactory.getLogger(ContactDb::class.java)

@Service
data class ContactDb(@param:Value($$"${contactsapp.db.file}") val file: Path) {
    init {
        logger.info("Using database at {}", file)
    }
}

@Repository
class SearchContacts(val loadAllContacts: LoadAllContacts) : ContactRepository {
    operator fun invoke(search: String): Set<Contact> =
        loadAllContacts().asSequence().filter { it.matches(search) }.toSet()
}

private val om: ObjectMapper = jacksonObjectMapper()

@Repository
class LoadAllContacts(val db: ContactDb) : ContactRepository {
    operator fun invoke(): Set<Contact> = om.readValue<Set<Contact>>(db.file.toFile())
}

@Repository
class SaveAllContacts(val db: ContactDb) : ContactRepository {
    operator fun invoke(contacts: Set<Contact>) {
        om.writeValue(db.file.toFile(), contacts)
    }
}
