package com.liveklass.presentation

import com.liveklass.application.EnrollmentTicketUseCase
import com.liveklass.application.WaitingQueueEmitter
import com.liveklass.dto.CreateWaitingTicketRequest
import com.liveklass.dto.EnterWaitingQueueResponse
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

@RestController
@RequestMapping("/api/v1")
class WaitingTicketController(
    private val enrollmentTicketUseCase: EnrollmentTicketUseCase,
    private val waitingQueueEmitter: WaitingQueueEmitter
) {
    @PostMapping("/classes/{classId}/waiting-tickets")
    fun create(
        @PathVariable classId: Long,
        @RequestBody request: CreateWaitingTicketRequest
    ): ResponseEntity<EnterWaitingQueueResponse> {
        val response = enrollmentTicketUseCase.enterWaitingQueue(classId, request.memberId)
        val location =
            ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/waiting-tickets/{id}")
                .buildAndExpand(response.ticketId)
                .toUri()
        return ResponseEntity.created(location).body(response)
    }

    @GetMapping("/waiting-tickets/{ticketId}")
    fun findDetail(@PathVariable ticketId: Long): ResponseEntity<EnterWaitingQueueResponse> =
        ResponseEntity.ok(enrollmentTicketUseCase.findCurrentWaiting(ticketId))

    @GetMapping("/waiting-tickets/{ticketId}/events", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun subscribe(@PathVariable ticketId: Long): SseEmitter {
        enrollmentTicketUseCase.findCurrentWaiting(ticketId)
        return SseEmitter(60_000L).also { waitingQueueEmitter.save(ticketId, it) }
    }
}
