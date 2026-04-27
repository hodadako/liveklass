package com.liveklass.presentation

import com.fasterxml.jackson.databind.ObjectMapper
import com.liveklass.application.EnrollmentRequestUseCase
import com.liveklass.application.EnrollmentUseCase
import com.liveklass.domain.EnrollmentStatus
import com.liveklass.dto.FindEnrollmentListResponse
import com.liveklass.dto.FindEnrollmentResponse
import com.liveklass.dto.RequestEnrollmentResponse
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.BDDMockito.willDoNothing
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime

@WebMvcTest(EnrollmentController::class)
@Import(ExceptionHandler::class)
@DisplayName("EnrollmentController 슬라이스 테스트")
class EnrollmentControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper
) {
    @MockitoBean
    private lateinit var enrollmentRequestUseCase: EnrollmentRequestUseCase

    @MockitoBean
    private lateinit var enrollmentUseCase: EnrollmentUseCase

    @Test
    fun `POST enrollments는 수강 신청을 생성한다`() {
        given(enrollmentRequestUseCase.requestEnrollment(1L, 2L, 3L)).willReturn(RequestEnrollmentResponse(10L))

        mockMvc.perform(
            post("/api/v1/classes/1/enrollments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mapOf("memberId" to 2L, "ticketId" to 3L)))
        )
            .andExpect(status().isCreated)
    }

    @Test
    fun `GET class enrollments는 강의별 전체 신청 목록을 조회한다`() {
        given(enrollmentUseCase.findEnrollmentRequestsByClass(1L)).willReturn(
            FindEnrollmentListResponse(listOf(enrollmentResponse(10L, EnrollmentStatus.PENDING)))
        )

        mockMvc.perform(get("/api/v1/classes/1/enrollments"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.responses[0].enrollmentId").value(10L))
            .andExpect(jsonPath("$.responses[0].enrollmentStatus").value("PENDING"))
    }

    @Test
    fun `GET member enrollments는 회원 기준 수강 신청 목록을 조회한다`() {
        given(enrollmentUseCase.findEnrollmentsByMember(2L)).willReturn(
            FindEnrollmentListResponse(listOf(enrollmentResponse(10L, EnrollmentStatus.CONFIRMED)))
        )

        mockMvc.perform(get("/api/v1/enrollments").queryParam("memberId", "2"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.responses[0].enrollmentId").value(10L))
            .andExpect(jsonPath("$.responses[0].enrollmentStatus").value("CONFIRMED"))
    }

    @Test
    fun `DELETE enrollment은 수강 신청을 취소한다`() {
        willDoNothing().given(enrollmentUseCase).cancelEnrollment(10L, 2L)

        mockMvc.perform(delete("/api/v1/enrollments/10").queryParam("memberId", "2"))
            .andExpect(status().isNoContent)

        then(enrollmentUseCase).should().cancelEnrollment(10L, 2L)
    }

    private fun enrollmentResponse(enrollmentId: Long, status: EnrollmentStatus) = FindEnrollmentResponse(
        enrollmentId = enrollmentId,
        classId = 1L,
        classTitle = "클래스",
        enrollmentStatus = status,
        requestedDate = LocalDateTime.of(2026, 4, 1, 10, 0),
        confirmedDate = LocalDateTime.of(2026, 4, 2, 10, 0),
        cancelledDate = null
    )
}
