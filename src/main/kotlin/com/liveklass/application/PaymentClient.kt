package com.liveklass.application

import org.springframework.stereotype.Component
import kotlin.random.Random

interface PaymentClient {
    fun requestPayment(enrollmentId: Long)
    fun cancelPayment(enrollmentId: Long)
}

@Component
class SimplePaymentClient(
    private val random: Random = Random.Default
) : PaymentClient {
    private val minDelay = 500L
    private val maxDelay = 2000L

    override fun requestPayment(enrollmentId: Long) {
        simulateDelay()
    }

    override fun cancelPayment(enrollmentId: Long) {
        simulateDelay()
    }

    private fun simulateDelay() {
        val delay = random.nextLong(minDelay, maxDelay)
        Thread.sleep(delay)
    }
}
