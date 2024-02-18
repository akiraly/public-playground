package org.example

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldNotBe

class AppTest : FunSpec({
  test("app should have a greeting") {
    val classUnderTest = App()
    classUnderTest.greeting shouldNotBe null
  }
})
