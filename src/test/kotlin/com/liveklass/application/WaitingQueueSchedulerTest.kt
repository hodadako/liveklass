package com.liveklass.application

import com.liveklass.domain.EnrollmentTicket
import com.liveklass.domain.EnrollmentTicketStatus
import com.liveklass.persistence.EnrollmentTicketRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable

@DisplayName("대기열 스케줄러 단위 테스트")
class WaitingQueueSchedulerTest {

    @Test
    fun `허용 가능한 대기 티켓이 있으면 allowed 이벤트를 티켓별로 전송한다`() {
        val waitingTickets = listOf(
            createTicket(1L, 1L),
            createTicket(1L, 2L)
        )
        val emitter = RecordingWaitingQueueEmitter()
        val repository = repositoryOf(waiting = waitingTickets)
        val scheduler = SimpleWaitingQueueScheduler(
            waitingQueueService = WaitingQueueService(repository),
            waitingQueueEmitter = emitter
        )

        scheduler.processTicketsAndSend()

        assertThat(emitter.allowedTicketIds)
            .containsExactlyElementsOf(waitingTickets.map { it.id })
    }

    @Test
    fun `만료 티켓 삭제 시 expired 이벤트를 티켓별로 전송한다`() {
        val expiredTickets = listOf(
            createTicket(1L, 1L, EnrollmentTicketStatus.EXPIRED),
            createTicket(1L, 2L, EnrollmentTicketStatus.EXPIRED)
        )
        val emitter = RecordingWaitingQueueEmitter()
        val repository = repositoryOf(expired = expiredTickets)
        val scheduler = SimpleWaitingQueueScheduler(
            waitingQueueService = WaitingQueueService(repository),
            waitingQueueEmitter = emitter
        )

        scheduler.deleteExpiredTickets()

        assertAll(
            {
                assertThat(emitter.expiredTicketIds)
                    .containsExactlyElementsOf(expiredTickets.map { it.id })
            },
            {
                verify(exactly = 1) {
                    repository.deleteAll(
                        match<Iterable<EnrollmentTicket>> {
                            it.map { ticket -> ticket.id } == expiredTickets.map { ticket -> ticket.id }
                        }
                    )
                }
            }
        )
    }

    private fun createTicket(
        classId: Long,
        memberId: Long,
        status: EnrollmentTicketStatus = EnrollmentTicketStatus.WAITING
    ): EnrollmentTicket {
        val ticket = EnrollmentTicket.create(classId, memberId)
        setTicketId(ticket, memberId)
        ticket.status = status
        return ticket
    }

    private fun setTicketId(ticket: EnrollmentTicket, ticketId: Long) {
        val field = ticket.javaClass.superclass.getDeclaredField("id")
        field.isAccessible = true
        field.set(ticket, ticketId)
    }

    private fun repositoryOf(
        waiting: List<EnrollmentTicket> = emptyList(),
        expired: List<EnrollmentTicket> = emptyList()
    ): EnrollmentTicketRepository {
        return mockk {
            every {
                findAllByStatus(EnrollmentTicketStatus.WAITING, any())
            } answers {
                val pageable = secondArg<Pageable>()
                pageOf(waiting, pageable)
            }

            every {
                findAllByStatus(EnrollmentTicketStatus.EXPIRED, any())
            } answers {
                val pageable = secondArg<Pageable>()
                pageOf(expired, pageable)
            }

            every {
                deleteAll(any<Iterable<EnrollmentTicket>>())
            } returns Unit
        }
    }

    private fun pageOf(
        tickets: List<EnrollmentTicket>,
        pageable: Pageable
    ): Page<EnrollmentTicket> {
        return PageImpl(
            tickets.take(pageable.pageSize),
            pageable,
            tickets.size.toLong()
        )
    }

    private class RecordingWaitingQueueEmitter : WaitingQueueEmitter {
        val allowedTicketIds = mutableListOf<Long>()
        val expiredTicketIds = mutableListOf<Long>()

        override fun save(
            ticketId: Long,
            emitter: org.springframework.web.servlet.mvc.method.annotation.SseEmitter
        ) = Unit

        override fun sendAllowed(ticketId: Long) {
            allowedTicketIds += ticketId
        }

        override fun sendExpired(ticketId: Long) {
            expiredTicketIds += ticketId
        }

        override fun complete(ticketId: Long) = Unit
    }
}
