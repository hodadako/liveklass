package com.liveklass.application

import com.liveklass.domain.EnrollmentTicket
import com.liveklass.domain.EnrollmentTicketStatus
import com.liveklass.dto.EnterWaitingQueueResponse
import com.liveklass.persistence.EnrollmentTicketRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface EnrollmentTicketUseCase {
    fun enterWaitingQueue(classId: Long, memberId: Long): EnterWaitingQueueResponse
    fun findCurrentWaiting(ticketId: Long): EnterWaitingQueueResponse
}

@Service
class EnrollmentTicketService(
    private val enrollmentTicketRepository: EnrollmentTicketRepository
) : EnrollmentTicketUseCase {

    @Transactional
    override fun enterWaitingQueue(
        classId: Long,
        memberId: Long
    ): EnterWaitingQueueResponse {
        val ticket = findOrCreateActiveTicket(classId, memberId)

        return buildResponse(ticket.id)
    }

    private fun findOrCreateActiveTicket(
        classId: Long,
        memberId: Long
    ): EnrollmentTicket {
        val existingTicket = enrollmentTicketRepository.findByClassIdAndMemberId(
            classId = classId,
            memberId = memberId
        )

        if (existingTicket != null) {
            return existingTicket
        }

        return enrollmentTicketRepository.save(
            EnrollmentTicket.create(
                classId = classId,
                memberId = memberId
            )
        )
    }

    @Transactional(readOnly = true)
    override fun findCurrentWaiting(
        ticketId: Long
    ): EnterWaitingQueueResponse {
        enrollmentTicketRepository.findByIdOrThrow(ticketId)
        return buildResponse(ticketId)
    }

    private fun buildResponse(ticketId: Long): EnterWaitingQueueResponse {
        val waitingCount = enrollmentTicketRepository.countByStatus(EnrollmentTicketStatus.WAITING)

        val position = enrollmentTicketRepository.findCurrentPosition(
            ticketId,
            EnrollmentTicketStatus.WAITING
        )

        return EnterWaitingQueueResponse(
            ticketId = ticketId,
            position = position,
            waitingCount = waitingCount,
            estimatedWaitingTime = estimateWaitingTime(position)
        )
    }

    private fun estimateWaitingTime(position: Long): Long {
        if (position <= 0) return 0
        return (position + QUEUE_PAGE_SIZE - 1) / QUEUE_PAGE_SIZE
    }
}
