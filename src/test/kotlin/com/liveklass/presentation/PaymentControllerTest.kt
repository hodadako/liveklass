package com.liveklass.presentation

import com.liveklass.application.PaymentUseCase
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.then
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(PaymentController::class)
@Import(ExceptionHandler::class)
@DisplayName("PaymentController 슬라이스 테스트")
class PaymentControllerTest @Autowired constructor(
    private val mockMvc: MockMvc
) {
    @MockitoBean
    private lateinit var paymentUseCase: PaymentUseCase

    @Test
    fun `POST payments request는 결제를 요청한다`() {
        mockMvc.perform(post("/api/v1/payments/enrollments/10/request"))
            .andExpect(status().isOk)

        then(paymentUseCase).should().requestPayment(10L)
    }

    @Test
    fun `POST payments cancel은 결제를 취소한다`() {
        mockMvc.perform(post("/api/v1/payments/enrollments/10/cancel"))
            .andExpect(status().isOk)

        then(paymentUseCase).should().cancelPayment(10L)
    }
}
