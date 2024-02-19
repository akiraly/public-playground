package org.example.logback

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
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


class AppLogbackConfigurator : ContextAwareBase(), Configurator {

  override fun configure(loggerContext: LoggerContext): ExecutionStatus {
    loggerContext.name = "contacts-htmx-http4k"

    val pattern = PatternLayout()
    pattern.context = loggerContext
    pattern.pattern = "%date{ISO8601, UTC} %logger %level [%thread] %msg%n"
    pattern.start()

    val encoder = LayoutWrappingEncoder<ILoggingEvent>()
    encoder.context = loggerContext
    encoder.charset = UTF_8
    encoder.layout = pattern
    encoder.start()

    val consoleAppender = ConsoleAppender<ILoggingEvent>()
    consoleAppender.context = loggerContext
    consoleAppender.name = "console"
    consoleAppender.encoder = encoder
    consoleAppender.start()

    val logger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME)
    logger.setLevel(Level.DEBUG)
    logger.addAppender(consoleAppender)

    StatusPrinter.print(loggerContext)

    return ExecutionStatus.DO_NOT_INVOKE_NEXT_IF_ANY
  }
}
