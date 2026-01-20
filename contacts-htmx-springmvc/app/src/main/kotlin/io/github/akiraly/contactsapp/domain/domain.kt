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

data class ContactUpsertRequest(
    val contactId: ContactId? = null,
    val first: String = "",
    val last: String = "",
    val phone: String = "",
    val email: String = ""
) {
    fun validate(): ValidatedContactUpsertRequest {
        val errors = mutableMapOf<ContactUpsertResult.Fields, String>()

        if (first.isBlank()) errors[ContactUpsertResult.Fields.First] = "First name is required"
        if (last.isBlank()) errors[ContactUpsertResult.Fields.Last] = "Last name is required"
        if (phone.isBlank()) errors[ContactUpsertResult.Fields.Phone] = "Phone is required"
        if (email.isBlank()) errors[ContactUpsertResult.Fields.Email] = "Email is required"

        return ValidatedContactUpsertRequest(this, errors)
    }
}

data class ValidatedContactUpsertRequest(
    val request: ContactUpsertRequest,
    val errors: Map<ContactUpsertResult.Fields, String> = emptyMap()
)

data class ContactUpsertResult(
    val request: ContactUpsertRequest = ContactUpsertRequest(),
    val contact: Contact? = null,
    val errors: Map<Fields, String> = emptyMap()
) {
    enum class Fields {
        First, Last, Phone, Email
    }
}

interface ContactRepository : org.jmolecules.ddd.types.Repository<Contact, ContactId>

@org.jmolecules.ddd.annotation.Service
interface ContactService
