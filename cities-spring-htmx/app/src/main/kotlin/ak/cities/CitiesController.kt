package ak.cities

import kotlinx.html.body
import kotlinx.html.h1
import kotlinx.html.head
import kotlinx.html.html
import kotlinx.html.lang
import kotlinx.html.meta
import kotlinx.html.stream.appendHTML
import kotlinx.html.title
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class CitiesController {

    @GetMapping("/")
    fun getCities(): ResponseEntity<String> {
        return ResponseEntity.ok(createHtml("Cities"))
    }
}

fun createHtml(title: String): String = buildString {
    appendLine("<!DOCTYPE html>")
    appendHTML().html {
        lang = "en"
        head {
            meta { charset = "UTF-8" }
            meta {
                name = "viewport"
                content = "width=device-width, initial-scale=1.0"
            }
            meta {
                httpEquiv = "X-UA-Compatible"
                content = "ie=edge"
            }
            title { +title }
        }
        body {
            h1 { +title }
        }
    }
}
