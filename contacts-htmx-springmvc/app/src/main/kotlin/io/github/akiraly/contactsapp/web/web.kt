package io.github.akiraly.contactsapp.web

import io.github.akiraly.contactsapp.repo.LoadAllContacts
import io.github.akiraly.contactsapp.repo.SearchContacts
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
class GetIndex {
    @GetMapping("/")
    operator fun invoke(): ResponseEntity<Void> =
        ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT).location(URI.create("/contacts"))
            .build()
}

@RestController
class GetContacts(
    val loadAllContacts: LoadAllContacts,
    val searchContacts: SearchContacts
) {
    @GetMapping("/contacts")
    operator fun invoke(): ResponseEntity<String> =
        ResponseEntity.ok().body("Contacts: Hello, World!")
}
