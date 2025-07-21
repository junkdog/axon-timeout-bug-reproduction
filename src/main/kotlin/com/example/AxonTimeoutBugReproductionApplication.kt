package com.example

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AxonTimeoutBugReproductionApplication

fun main(args: Array<String>) {
    runApplication<AxonTimeoutBugReproductionApplication>(*args)
}