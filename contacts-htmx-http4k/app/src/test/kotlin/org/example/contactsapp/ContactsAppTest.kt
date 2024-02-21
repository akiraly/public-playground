package org.example.contactsapp

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldNotBe

class ContactsAppTest : FunSpec({
  test("app should have a greeting") {
    val classUnderTest = ContactsApp()
    classUnderTest.greeting shouldNotBe null
  }
})
