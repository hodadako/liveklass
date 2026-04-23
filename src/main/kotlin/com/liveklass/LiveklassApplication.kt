package com.liveklass

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class LiveklassApplication

fun main(args: Array<String>) {
    runApplication<LiveklassApplication>(*args)
}