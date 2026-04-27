package com.liveklass.presentation

import com.liveklass.application.EnrollmentRequestUseCase
import com.liveklass.application.EnrollmentUseCase
import com.liveklass.dto.CreateEnrollmentRequest
import com.liveklass.dto.FindEnrollmentListResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
class EnrollmentController(
    private val enrollmentRequestUseCase: EnrollmentRequestUseCase,
    private val enrollmentUseCase: EnrollmentUseCase
) {
    @PostMapping("/classes/{classId}/enrollments")
    fun create(
        @PathVariable classId: Long,
        @RequestBody request: CreateEnrollmentRequest
    ): ResponseEntity<Unit> {
        enrollmentRequestUseCase.requestEnrollment(classId, request.memberId, request.ticketId)
        return ResponseEntity.status(201).build()
    }

    @GetMapping("/classes/{classId}/enrollments")
    fun findByClass(@PathVariable classId: Long): ResponseEntity<FindEnrollmentListResponse> =
        ResponseEntity.ok(enrollmentUseCase.findEnrollmentRequestsByClass(classId))

    @GetMapping("/enrollments")
    fun findByMember(@RequestParam memberId: Long): ResponseEntity<FindEnrollmentListResponse> =
        ResponseEntity.ok(enrollmentUseCase.findEnrollmentsByMember(memberId))

    @DeleteMapping("/enrollments/{enrollmentId}")
    fun cancel(
        @PathVariable enrollmentId: Long,
        @RequestParam memberId: Long
    ): ResponseEntity<Unit> {
        enrollmentUseCase.cancelEnrollment(enrollmentId, memberId)
        return ResponseEntity.noContent().build()
    }
}
