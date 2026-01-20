package io.github.akiraly.contactsapp.domain

import io.github.akiraly.contactsapp.repo.LoadAllContacts
import io.github.akiraly.contactsapp.repo.SaveAllContacts
import org.springframework.stereotype.Service
import java.util.*

@Service
class UpsertContact(
    private val loadAllContacts: LoadAllContacts,
    private val saveAllContacts: SaveAllContacts
) : ContactService {
    operator fun invoke(upsertRequest: ContactUpsertRequest): ContactUpsertResult {
        val validatedRequest = upsertRequest.validate()

        if (validatedRequest.errors.isNotEmpty()) {
            return ContactUpsertResult(
                request = validatedRequest.request,
                errors = validatedRequest.errors
            )
        }

        val contacts = loadAllContacts()
            .groupBy { it.id }
            .mapValuesTo(mutableMapOf()) { it.value.single() }

        val contactId =
            validatedRequest.request.contactId ?: ContactId(UUID.randomUUID().toString())

        val contact = Contact(
            contactId,
            validatedRequest.request.first,
            validatedRequest.request.last,
            validatedRequest.request.phone,
            validatedRequest.request.email
        )
        contacts[contactId] = contact

        saveAllContacts(contacts.values.toSet())

        return ContactUpsertResult(
            request = validatedRequest.request,
            contact = contact,
            errors = emptyMap()
        )
    }
}
