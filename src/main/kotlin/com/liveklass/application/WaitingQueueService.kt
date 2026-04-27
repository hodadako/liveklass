package com.liveklass.application

import com.liveklass.domain.EnrollmentTicketStatus
import com.liveklass.persistence.EnrollmentTicketRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

const val QUEUE_PAGE_SIZE = 10

interface WaitingQueueUseCase {
    fun findWaitingTickets(): List<Long>
    fun deleteExpiredTickets(): List<Long>
}

@Service
class WaitingQueueService(
    private val enrollmentTicketRepository: EnrollmentTicketRepository
) : WaitingQueueUseCase {
    @Transactional
    override fun findWaitingTickets(): List<Long> {
        val now = LocalDateTime.now()

        val tickets = enrollmentTicketRepository.findAllByStatus(
            EnrollmentTicketStatus.WAITING,
            PageRequest.of(0, QUEUE_PAGE_SIZE)
        )

        tickets.content.forEach {
            it.allow(now, 5)
        }

        return tickets.content.map {
            it.id
        }
    }

    @Transactional
    override fun deleteExpiredTickets(): List<Long> {
        val tickets = enrollmentTicketRepository.findAllByStatus(
            EnrollmentTicketStatus.EXPIRED,
            PageRequest.of(0, QUEUE_PAGE_SIZE)
        )
        enrollmentTicketRepository.deleteAll(
            tickets
        )
        return tickets.content.map {
            it.id
        }
    }
}
