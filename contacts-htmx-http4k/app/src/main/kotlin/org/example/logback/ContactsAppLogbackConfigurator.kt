package org.example.logback

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.PatternLayout
import ch.qos.logback.classic.spi.Configurator
import ch.qos.logback.classic.spi.Configurator.ExecutionStatus
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.encoder.LayoutWrappingEncoder
import ch.qos.logback.core.spi.ContextAwareBase
import ch.qos.logback.core.util.StatusPrinter
import kotlin.text.Charsets.UTF_8


class ContactsAppLogbackConfigurator : ContextAwareBase(), Configurator {

  override fun configure(loggerContext: LoggerContext): ExecutionStatus = loggerContext.doConfigure()

  private fun LoggerContext.doConfigure(): ExecutionStatus {
    name = "contactsapp"

    val consoleAppender = consoleAppender {
      encoder = layoutWrappingEncoder {
        layout = patternLayout()
      }
    }

    with(getLogger(ROOT_LOGGER_NAME)) {
      level = Level.DEBUG
      addAppender(consoleAppender)
    }

    StatusPrinter.print(this)

    return ExecutionStatus.DO_NOT_INVOKE_NEXT_IF_ANY
  }
}

context (LoggerContext)
private fun consoleAppender(buildFn: ConsoleAppender<ILoggingEvent>.() -> Unit): ConsoleAppender<ILoggingEvent> {
  val appender = ConsoleAppender<ILoggingEvent>()
  appender.context = this@LoggerContext
  appender.buildFn()
  appender.start()
  return appender
}

context (LoggerContext)
private fun layoutWrappingEncoder(
  buildFn: LayoutWrappingEncoder<ILoggingEvent>.() -> Unit
): LayoutWrappingEncoder<ILoggingEvent> {
  val encoder = LayoutWrappingEncoder<ILoggingEvent>()
  encoder.context = this@LoggerContext
  encoder.charset = UTF_8
  encoder.buildFn()
  encoder.start()
  return encoder
}

context (LoggerContext)
private fun patternLayout(
  buildFn: PatternLayout.() -> Unit = {
    pattern = "%date{ISO8601, UTC} %level %logger [%thread] %msg%n"
  }
): PatternLayout {
  val patternLayout = PatternLayout()
  patternLayout.context = this@LoggerContext
  patternLayout.buildFn()
  patternLayout.start()
  return patternLayout
}
