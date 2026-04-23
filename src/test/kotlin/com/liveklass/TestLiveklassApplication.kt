package com.liveklass

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
    fromApplication<LiveklassApplication>().with(TestcontainersConfiguration::class).run(*args)
}
