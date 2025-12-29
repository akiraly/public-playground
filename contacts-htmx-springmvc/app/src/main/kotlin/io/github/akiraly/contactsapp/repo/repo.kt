package io.github.akiraly.contactsapp.repo

import io.github.akiraly.contactsapp.domain.Contact
import io.github.akiraly.contactsapp.domain.ContactId
import org.jmolecules.ddd.types.Repository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.nio.file.Path

val logger: Logger = LoggerFactory.getLogger(ContactDb::class.java)

@Service
data class ContactDb(@param:Value($$"${contactsapp.db.file}") val file: Path) {
    init {
        logger.info("Using database at {}", file)
    }
}

@org.springframework.stereotype.Repository
class SearchContacts(val db: ContactDb) : Repository<Contact, ContactId> {
    operator fun invoke(search: String): Set<Contact> {
        TODO()
    }
}

@org.springframework.stereotype.Repository
class LoadAllContacts(val db: ContactDb) : Repository<Contact, ContactId> {
    operator fun invoke(): Set<Contact> {
        TODO()
    }
}
