package org.example.contactsapp

import io.kotest.core.spec.style.FunSpec
import org.http4k.contract.contract
import org.http4k.contract.meta
import org.http4k.contract.openapi.ApiInfo
import org.http4k.contract.openapi.v3.OpenApi3
import org.http4k.core.*
import org.http4k.filter.CorsPolicy
import org.http4k.filter.ServerFilters
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.routing.webJars
import org.http4k.server.JettyLoom
import org.http4k.server.asServer
import org.junit.jupiter.api.Assertions.assertEquals

fun helloHandler(request: Request): Response {
  return Response(Status.OK).body("Hello, HTTP4k!")
}

fun echoHandler(request: Request): Response {
  val body = request.bodyString()
  return Response(Status.OK).body("You said: $body")
}

class Test : FunSpec({

  test("helloHandler should return correct response") {
    val request = Request(Method.GET, "/hello")
    val response = helloHandler(request)
    assertEquals(Status.OK, response.status)
    assertEquals("Hello, HTTP4k!", response.bodyString())
  }

  test("echoHandler should return correct response") {
    val requestBody = "Testing echo"
    val request = Request(Method.POST, "/echo").body(requestBody)
    val response = echoHandler(request)
    assertEquals(Status.OK, response.status)
    assertEquals("You said: $requestBody", response.bodyString())
  }
})

fun main() {
  val openApi = OpenApi3(
    ApiInfo(
      "Sample API",
      "1.0.0",
      "A sample API for demonstration purposes"
    )
  )

  val documentedApp = contract {
    renderer = openApi

    routes += "/hello" meta {
      summary = "Say hello"
    } bindContract Method.GET to ::helloHandler

    routes += "/echo" meta {
      summary = "Echo a message"
    } bindContract Method.POST to ::echoHandler
  }

  val fullApp = routes(
    "/" bind documentedApp,
    webJars()
  )

  val filteredApp = ServerFilters.Cors(CorsPolicy.UnsafeGlobalPermissive).then(fullApp)

  filteredApp.asServer(JettyLoom(9000)).start()
}
