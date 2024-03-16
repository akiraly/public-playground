package org.example.contactsapp

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldNotBe
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.server.Http4kServer
import org.http4k.server.JettyLoom
import org.http4k.server.asServer

class ContactsAppTest : FunSpec({
  test("app should have a greeting") {
    val classUnderTest = ContactsApp()
    classUnderTest.greeting shouldNotBe null
  }

  test("greeting over http should work as expected") {

  }
})


fun MyMathServer(port: Int): Http4kServer =
  { _: Request -> Response(Status.OK) }.asServer(JettyLoom(port))
