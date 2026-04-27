package com.liveklass.presentation

import com.liveklass.application.PaymentUseCase
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/payments")
class PaymentController(
    private val paymentUseCase: PaymentUseCase
) {
    @PostMapping("/enrollments/{enrollmentId}/request")
    fun requestPayment(@PathVariable enrollmentId: Long): ResponseEntity<Unit> {
        paymentUseCase.requestPayment(enrollmentId)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/enrollments/{enrollmentId}/cancel")
    fun cancelPayment(@PathVariable enrollmentId: Long): ResponseEntity<Unit> {
        paymentUseCase.cancelPayment(enrollmentId)
        return ResponseEntity.ok().build()
    }
}
