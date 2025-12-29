package io.github.akiraly.contactsapp.domain

import org.jmolecules.ddd.types.AggregateRoot
import org.jmolecules.ddd.types.Identifier
import org.jmolecules.ddd.types.ValueObject

data class ContactId(val value: String) : Identifier, ValueObject

data class Contact(
    override val id: ContactId,
    val name: String
) : AggregateRoot<Contact, ContactId>
