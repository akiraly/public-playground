package io.github.akiraly.contactsapp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ContactsApp

fun main(args: Array<String>) {
    runApplication<ContactsApp>(*args)
}
