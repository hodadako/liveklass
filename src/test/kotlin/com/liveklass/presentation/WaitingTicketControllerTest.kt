package com.liveklass.presentation

import com.fasterxml.jackson.databind.ObjectMapper
import com.liveklass.application.EnrollmentTicketUseCase
import com.liveklass.application.WaitingQueueEmitter
import com.liveklass.dto.EnterWaitingQueueResponse
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.request
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(WaitingTicketController::class)
@Import(ExceptionHandler::class)
@DisplayName("WaitingTicketController 슬라이스 테스트")
class WaitingTicketControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper
) {
    @MockitoBean
    private lateinit var enrollmentTicketUseCase: EnrollmentTicketUseCase

    @MockitoBean
    private lateinit var waitingQueueEmitter: WaitingQueueEmitter

    @Test
    fun `POST waiting tickets는 대기열 티켓을 생성한다`() {
        given(enrollmentTicketUseCase.enterWaitingQueue(1L, 2L)).willReturn(waitingResponse())

        mockMvc.perform(
            post("/api/v1/classes/1/waiting-tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mapOf("memberId" to 2L)))
        )
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", "http://localhost/api/v1/waiting-tickets/11"))
            .andExpect(jsonPath("$.ticketId").value(11L))
            .andExpect(jsonPath("$.position").value(3L))
    }

    @Test
    fun `GET waiting tickets detail은 현재 대기열 정보를 조회한다`() {
        given(enrollmentTicketUseCase.findCurrentWaiting(11L)).willReturn(waitingResponse())

        mockMvc.perform(get("/api/v1/waiting-tickets/11"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.ticketId").value(11L))
            .andExpect(jsonPath("$.waitingCount").value(20L))
    }

    @Test
    fun `GET waiting ticket events는 SSE 구독을 생성한다`() {
        given(enrollmentTicketUseCase.findCurrentWaiting(11L)).willReturn(waitingResponse())

        mockMvc.perform(get("/api/v1/waiting-tickets/11/events"))
            .andExpect(request().asyncStarted())
            .andExpect(status().isOk)
    }

    private fun waitingResponse() = EnterWaitingQueueResponse(
        ticketId = 11L,
        position = 3L,
        waitingCount = 20L,
        estimatedWaitingTime = 1L
    )
}
