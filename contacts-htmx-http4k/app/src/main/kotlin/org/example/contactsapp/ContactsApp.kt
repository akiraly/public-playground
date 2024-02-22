package org.example.contactsapp

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.PatternLayout
import ch.qos.logback.classic.spi.Configurator
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.encoder.LayoutWrappingEncoder
import ch.qos.logback.core.spi.ContextAwareBase
import ch.qos.logback.core.util.StatusPrinter
import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addResourceSource
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger(ContactsApp::class.java)
fun main() {
  logger.info(ContactsApp().greeting)
}

val contactsAppConfig = lazy { readContactsAppConfig() }

class ContactsApp {
  val greeting: String
    get() {
      return "Hello World!"
    }
}

enum class ContactsAppEnv {
  Dev, QA, Prod;

  companion object {
    const val APP_ENV = "APP_ENV"

    private val values = ContactsAppEnv.entries.associateBy { it.name.lowercase() }
    fun valueOfIgnoreCase(value: String): ContactsAppEnv = values.getValue(value.lowercase())

    fun readFromEnv(env: Map<String, String> = System.getenv()): ContactsAppEnv =
      valueOfIgnoreCase(env.getValue(APP_ENV))
  }
}

data class ContactsAppConfig(val appEnv: ContactsAppEnv)

fun readContactsAppConfig(
  fileNameStub: String = "application",
  env: ContactsAppEnv = ContactsAppEnv.readFromEnv()
): ContactsAppConfig {
  return ConfigLoaderBuilder.default()
    .withResolveTypesCaseInsensitive()
    .addResourceSource("/$fileNameStub-${env.name.lowercase()}.yml")
    .addResourceSource("/$fileNameStub.yml")
    .build()
    .loadConfigOrThrow<ContactsAppConfig>()
}

class ContactsAppLogbackConfigurator : ContextAwareBase(), Configurator {

  override fun configure(loggerContext: LoggerContext): Configurator.ExecutionStatus {
    loggerContext.configure()
    return Configurator.ExecutionStatus.DO_NOT_INVOKE_NEXT_IF_ANY
  }
}

private fun LoggerContext.configure() {
  name = "contactsapp"

  val consoleAppender = consoleAppender {
    encoder = layoutWrappingEncoder {
      layout = patternLayout()
    }
  }

  getLogger(Logger.ROOT_LOGGER_NAME).apply {
    level = Level.DEBUG
    addAppender(consoleAppender)
  }

  StatusPrinter.print(this)
}

private fun LoggerContext.consoleAppender(
  buildFn: ConsoleAppender<ILoggingEvent>.() -> Unit
): ConsoleAppender<ILoggingEvent> {
  val appender = ConsoleAppender<ILoggingEvent>()
  appender.context = this
  appender.buildFn()
  appender.start()
  return appender
}

private fun LoggerContext.layoutWrappingEncoder(
  buildFn: LayoutWrappingEncoder<ILoggingEvent>.() -> Unit
): LayoutWrappingEncoder<ILoggingEvent> {
  val encoder = LayoutWrappingEncoder<ILoggingEvent>()
  encoder.context = this
  encoder.charset = Charsets.UTF_8
  encoder.buildFn()
  encoder.start()
  return encoder
}

private fun LoggerContext.patternLayout(
  buildFn: PatternLayout.() -> Unit = {
    pattern = "%date{ISO8601, UTC} %level %logger [%thread] %msg%n"
  }
): PatternLayout {
  val patternLayout = PatternLayout()
  patternLayout.context = this
  patternLayout.buildFn()
  patternLayout.start()
  return patternLayout
}
