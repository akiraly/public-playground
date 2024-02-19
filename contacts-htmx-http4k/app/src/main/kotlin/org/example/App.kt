package org.example

import org.slf4j.LoggerFactory

class App {
  val greeting: String
    get() {
      return "Hello World!"
    }
}

private val logger = LoggerFactory.getLogger(App::class.java)
fun main() {
  logger.info(App().greeting)
}
