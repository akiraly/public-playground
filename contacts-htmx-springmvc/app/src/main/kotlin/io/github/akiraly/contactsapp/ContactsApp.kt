package io.github.akiraly.contactsapp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@SpringBootApplication
class ContactsApp

fun main(args: Array<String>) {
    runApplication<ContactsApp>(*args)
}

@RestController
class GetIndex {

    @GetMapping("/")
    operator fun invoke(): ResponseEntity<Void> =
        ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT).location(URI.create("/contacts"))
            .build()
}

@RestController
class GetContacts {

    @GetMapping("/contacts")
    operator fun invoke(): ResponseEntity<String> =
        ResponseEntity.ok().body("Contacts: Hello, World!")
}
