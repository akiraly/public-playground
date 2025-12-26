package io.github.akiraly.contactsapp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Service
import org.springframework.web.servlet.function.RequestPredicates.GET
import org.springframework.web.servlet.function.RouterFunction
import org.springframework.web.servlet.function.RouterFunctions.route
import org.springframework.web.servlet.function.ServerRequest
import org.springframework.web.servlet.function.ServerResponse
import java.net.URI

@SpringBootApplication
class ContactsApp {
    @Bean
    fun router(webEndpoints: List<RoutableWebEndpoint>): RouterFunction<ServerResponse> =
        webEndpoints.fold(route()) { r, we -> r.add(we.route()) }.build()
}

fun main(args: Array<String>) {
    runApplication<ContactsApp>(*args)
}

fun interface RoutableWebEndpoint {
    fun route(): RouterFunction<ServerResponse>
}

@Service
class GetIndex : RoutableWebEndpoint {
    override fun route(): RouterFunction<ServerResponse> =
        route(GET("/"), ::invoke)

    operator fun invoke(request: ServerRequest): ServerResponse =
        ServerResponse.temporaryRedirect(URI.create("/contacts")).build()
}

@Service
class GetContacts : RoutableWebEndpoint {
    override fun route(): RouterFunction<ServerResponse> =
        route(GET("/contacts"), ::invoke)

    operator fun invoke(request: ServerRequest): ServerResponse =
        ServerResponse.ok().body("Contacts: Hello, World!")
}
