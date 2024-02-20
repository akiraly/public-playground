package org.example

import org.slf4j.LoggerFactory

class ContactsApp {
  val greeting: String
    get() {
      return "Hello World!"
    }
}

private val logger = LoggerFactory.getLogger(ContactsApp::class.java)
fun main() {
  logger.info(ContactsApp().greeting)
}
