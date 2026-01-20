package io.github.akiraly.contactsapp.web

import io.github.akiraly.contactsapp.domain.Contact
import io.github.akiraly.contactsapp.domain.ContactUpsertRequest
import io.github.akiraly.contactsapp.domain.ContactUpsertResult
import io.github.akiraly.contactsapp.domain.UpsertContact
import io.github.akiraly.contactsapp.repo.LoadAllContacts
import io.github.akiraly.contactsapp.repo.SearchContacts
import kotlinx.html.FlowContent
import kotlinx.html.FormMethod
import kotlinx.html.InputType
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.button
import kotlinx.html.fieldSet
import kotlinx.html.form
import kotlinx.html.h1
import kotlinx.html.head
import kotlinx.html.header
import kotlinx.html.html
import kotlinx.html.id
import kotlinx.html.input
import kotlinx.html.label
import kotlinx.html.lang
import kotlinx.html.legend
import kotlinx.html.link
import kotlinx.html.main
import kotlinx.html.p
import kotlinx.html.script
import kotlinx.html.span
import kotlinx.html.stream.createHTML
import kotlinx.html.table
import kotlinx.html.tbody
import kotlinx.html.td
import kotlinx.html.th
import kotlinx.html.thead
import kotlinx.html.title
import kotlinx.html.tr
import kotlinx.html.unsafe
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
class GetIndexPage {
    @GetMapping("/")
    operator fun invoke(): ResponseEntity<Void> = redirectToContactListPage()
}

private fun redirectToContactListPage(): ResponseEntity<Void> =
    ResponseEntity
        .status(HttpStatus.TEMPORARY_REDIRECT)
        .location(URI.create("/contacts"))
        .build()

@RestController
class GetContactListPage(
    val loadAllContacts: LoadAllContacts,
    val searchContacts: SearchContacts
) {
    @GetMapping("/contacts")
    operator fun invoke(
        @RequestParam("q", required = false) search: String? = null
    ): ResponseEntity<String> {
        val contacts = if (search != null) searchContacts(search) else loadAllContacts()
        return ResponseEntity.ok().body(buildContactListPage(search, contacts))
    }
}

fun buildContactListPage(search: String?, contacts: Set<Contact>): String =
    buildContactsAppHTML {
        form(action = "/contacts", method = FormMethod.get, classes = "tool-bar") {
            label {
                htmlFor = "search"
                +"Search Term"
            }
            input(type = InputType.search, name = "q") {
                id = "search"
                value = search ?: ""
            }
            input(type = InputType.submit) {
                value = "Search"
            }
        }
        table {
            thead {
                tr {
                    th { +"First" }
                    th { +"Last" }
                    th { +"Phone" }
                    th { +"Email" }
                    th {}
                }
            }
            tbody {
                contacts.forEach { contact ->
                    tr {
                        td { +contact.first }
                        td { +contact.last }
                        td { +contact.phone }
                        td { +contact.email }
                        td {
                            a(href = "/contacts/${contact.id}/edit") { +"Edit" }
                            +" "
                            a(href = "/contacts/${contact.id}") { +"View" }
                        }
                    }
                }
            }
        }
        p {
            a(href = "/contacts/new") { +"Add Contact" }
        }
    }

@RestController
class GetNewContactPage {
    @GetMapping("/contacts/new")
    operator fun invoke(): ResponseEntity<String> {
        return ResponseEntity.ok().body(buildNewContactPage(ContactUpsertResult()))
    }
}

@RestController
class PostContactUpsertRequest(
    val upsertContact: UpsertContact
) {
    @PostMapping("/contacts/new")
    operator fun invoke(@ModelAttribute contactUpsertRequest: ContactUpsertRequest): ResponseEntity<out Any> {
        val result = upsertContact(contactUpsertRequest)
        return if (result.errors.isNotEmpty())
            ResponseEntity.badRequest().body(buildNewContactPage(result))
        else
            redirectToContactListPage()
    }
}

fun buildNewContactPage(result: ContactUpsertResult): String =
    buildContactsAppHTML {
        form(action = "/contacts/new", method = FormMethod.post) {
            fieldSet {
                legend { +"Contact Values" }
                p {
                    label {
                        htmlFor = "email"
                        +"Email"
                    }
                    input(type = InputType.email, name = "email") {
                        id = "email"
                        placeholder = "Email"
                        value = result.request.email
                    }
                    span(classes = "error") {
                        +(result.errors[ContactUpsertResult.Fields.Email] ?: "")
                    }
                }
                p {
                    label {
                        htmlFor = "first"
                        +"First Name"
                    }
                    input(type = InputType.text, name = "first") {
                        id = "first"
                        placeholder = "First Name"
                        value = result.request.first
                    }
                    span(classes = "error") {
                        +(result.errors[ContactUpsertResult.Fields.First] ?: "")
                    }
                }
                p {
                    label {
                        htmlFor = "last"
                        +"Last Name"
                    }
                    input(type = InputType.text, name = "last") {
                        id = "last"
                        placeholder = "Last Name"
                        value = result.request.last
                    }
                    span(classes = "error") {
                        +(result.errors[ContactUpsertResult.Fields.Last] ?: "")
                    }
                }
                p {
                    label {
                        htmlFor = "phone"
                        +"Phone"
                    }
                    input(type = InputType.text, name = "phone") {
                        id = "phone"
                        placeholder = "Phone"
                        value = result.request.phone
                    }
                    span(classes = "error") {
                        +(result.errors[ContactUpsertResult.Fields.Phone] ?: "")
                    }
                }

                button { +"Save" }
            }
        }

        p {
            a(href = "/contacts") { +"Back" }
        }
    }

fun buildContactsAppHTML(content: FlowContent.() -> Unit): String {

    return "<!doctype html>\n" + createHTML().html {
        lang = "en"
        head {
            title { +"Contact App" }
            link(rel = "stylesheet", href = "https://unpkg.com/missing.css@1.2.0")
            link(rel = "stylesheet", href = "/site.css")
            script(src = "/webjars/htmx.org/2.0.8/dist/htmx.min.js") {}
            script(src = "/webjars/hyperscript.org/0.9.14/dist/_hyperscript.min.js") {}
            script(src = "/js/rsjs-menu.js") {
                attributes["type"] = "module"
            }
            script(src = "/webjars/alpinejs/3.15.3/dist/cdn.min.js") {
                defer = true
            }
        }
        body {
            main {
                header {
                    h1 {
                        unsafe {
                            raw(
                                """
                                <all-caps>contacts.app</all-caps>
                                <sub-title>A Demo Contacts Application</sub-title>
                                """.trimIndent()
                            )
                        }
                    }
                }

                content()
            }
        }
    }.trim()
}
