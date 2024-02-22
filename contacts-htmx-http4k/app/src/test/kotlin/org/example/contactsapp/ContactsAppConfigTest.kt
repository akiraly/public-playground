package org.example.contactsapp

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.example.contactsapp.ContactsAppEnv.Companion.APP_ENV

class ContactsAppConfigTest : FunSpec({

  test("test that app config can be read properly") {
    readContactsAppConfig("contactsAppConfigTest", ContactsAppEnv.Dev) shouldBe ContactsAppConfig(
      appEnv = ContactsAppEnv.Dev
    )
  }

  test("it should be possible to read ContactsAppEnv from the system environment") {
    listOf("dev", "Dev", "DEV").forEach { v ->
      ContactsAppEnv.valueOfIgnoreCase(v) shouldBe ContactsAppEnv.Dev

      ContactsAppEnv.readFromEnv(mapOf(APP_ENV to v)) shouldBe ContactsAppEnv.Dev
    }

    listOf("qa", "Qa", "QA").forEach { v ->
      ContactsAppEnv.valueOfIgnoreCase(v) shouldBe ContactsAppEnv.QA

      ContactsAppEnv.readFromEnv(mapOf(APP_ENV to v)) shouldBe ContactsAppEnv.QA
    }
  }
})
