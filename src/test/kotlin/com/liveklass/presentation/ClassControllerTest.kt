package com.liveklass.presentation

import com.fasterxml.jackson.databind.ObjectMapper
import com.liveklass.application.ClassUseCase
import com.liveklass.domain.ClassStatus
import com.liveklass.dto.CreateClassRequest
import com.liveklass.dto.FindClassListResponse
import com.liveklass.dto.FindClassResponse
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime

@WebMvcTest(ClassController::class)
@Import(ExceptionHandler::class)
@DisplayName("ClassController 슬라이스 테스트")
class ClassControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper
) {
    @MockitoBean
    private lateinit var classUseCase: ClassUseCase

    @Test
    fun `GET classes는 상태 필터로 강의 목록을 조회한다`() {
        given(classUseCase.findClasses(ClassStatus.OPEN)).willReturn(
            FindClassListResponse(
                listOf(
                    FindClassResponse(
                        classId = 1L,
                        title = "클래스",
                        description = "설명",
                        classStatus = ClassStatus.OPEN,
                        price = 1000L,
                        enrolledCount = 3L,
                        startDate = LocalDateTime.of(2026, 4, 1, 10, 0),
                        endDate = LocalDateTime.of(2026, 4, 30, 10, 0)
                    )
                )
            )
        )

        mockMvc.perform(get("/api/v1/classes").queryParam("status", "OPEN"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.responses[0].classId").value(1L))
            .andExpect(jsonPath("$.responses[0].classStatus").value("OPEN"))
    }

    @Test
    fun `GET classes detail은 강의 상세를 조회한다`() {
        given(classUseCase.findClassDetail(1L)).willReturn(
            FindClassResponse(
                classId = 1L,
                title = "클래스",
                description = "설명",
                classStatus = ClassStatus.OPEN,
                price = 1000L,
                enrolledCount = 3L,
                startDate = LocalDateTime.of(2026, 4, 1, 10, 0),
                endDate = LocalDateTime.of(2026, 4, 30, 10, 0)
            )
        )

        mockMvc.perform(get("/api/v1/classes/1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.classId").value(1L))
            .andExpect(jsonPath("$.title").value("클래스"))
    }

    @Test
    fun `POST classes는 강의를 생성하고 location을 반환한다`() {
        given(classUseCase.createClass(createClassRequest())).willReturn(1L)

        mockMvc.perform(
            post("/api/v1/classes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createClassRequest()))
        )
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", "http://localhost/api/v1/classes/1"))
    }

    @Test
    fun `PATCH classes status는 강의 상태를 변경한다`() {
        willDoNothing().given(classUseCase).openClass(1L)

        mockMvc.perform(
            patch("/api/v1/classes/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"classStatus":"OPEN"}""")
        )
            .andExpect(status().isNoContent)

        then(classUseCase).should().openClass(1L)
    }

    @Test
    fun `PATCH classes status에서 DRAFT 요청은 400을 반환한다`() {
        mockMvc.perform(
            patch("/api/v1/classes/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"classStatus":"DRAFT"}""")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("DRAFT 상태로 변경하는 API는 지원하지 않습니다."))
    }

    private fun createClassRequest() = CreateClassRequest(
        title = "클래스",
        description = "설명",
        price = 1000L,
        capacity = 30L,
        startDate = LocalDateTime.of(2026, 4, 1, 10, 0),
        endDate = LocalDateTime.of(2026, 4, 30, 10, 0)
    )
}
