package com.liveklass.application

import org.springframework.stereotype.Service

interface PaymentUseCase {
    fun requestPayment(enrollmentId: Long)
    fun cancelPayment(enrollmentId: Long)
}

@Service
class PaymentService(
    private val paymentClient: PaymentClient
) : PaymentUseCase {
    override fun requestPayment(enrollmentId: Long) {
        paymentClient.requestPayment(enrollmentId)
    }

    override fun cancelPayment(enrollmentId: Long) {
        paymentClient.cancelPayment(enrollmentId)
    }
}
