package io.github.akiraly.sghbc

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SimpleGradleHTTPBuildCacheApplication

fun main(args: Array<String>) {
    runApplication<SimpleGradleHTTPBuildCacheApplication>(*args)
}
