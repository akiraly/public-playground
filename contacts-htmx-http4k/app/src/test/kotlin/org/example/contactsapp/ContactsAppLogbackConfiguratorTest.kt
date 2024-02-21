package org.example.contactsapp

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.core.ConsoleAppender
import io.kotest.core.spec.style.FunSpec
import io.kotest.inspectors.forExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ContactsAppLogbackConfiguratorTest : FunSpec({

  test("config test") {
    val loggerFactory = LoggerFactory.getILoggerFactory()
    loggerFactory.shouldBeInstanceOf<LoggerContext>()
    loggerFactory.name shouldBe "contactsapp"

    loggerFactory.getLogger(Logger.ROOT_LOGGER_NAME).iteratorForAppenders().asSequence().forExactly(1) {
      it.shouldBeInstanceOf<ConsoleAppender<*>>()
    }
  }
})
