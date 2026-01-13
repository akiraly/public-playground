package io.github.akiraly.contactsapp.domain

import com.fasterxml.jackson.annotation.JsonValue
import org.jmolecules.ddd.types.AggregateRoot
import org.jmolecules.ddd.types.Identifier
import org.jmolecules.ddd.types.ValueObject

data class ContactId(@JsonValue val value: String) : Identifier, ValueObject

data class Contact(
    override val id: ContactId,
    val first: String,
    val last: String,
    val phone: String,
    val email: String
) : AggregateRoot<Contact, ContactId> {
    fun matches(search: String): Boolean =
        first.contains(search, ignoreCase = true)
            || last.contains(search, ignoreCase = true)
            || phone.contains(search, ignoreCase = true)
            || email.contains(search, ignoreCase = true)
}
