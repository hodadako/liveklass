package com.liveklass.application

import com.liveklass.domain.EnrollmentTicket
import com.liveklass.domain.EnrollmentTicketStatus
import com.liveklass.persistence.EnrollmentTicketRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import java.lang.reflect.Proxy

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
            waitingQueueService = WaitingQueueService(repository.proxy),
            waitingQueueEmitter = emitter
        )

        scheduler.processTicketsAndSend()

        assertThat(emitter.allowedTicketIds).containsExactlyElementsOf(waitingTickets.map { it.id })
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
            waitingQueueService = WaitingQueueService(repository.proxy),
            waitingQueueEmitter = emitter
        )

        scheduler.deleteExpiredTickets()

        assertAll(
            { assertThat(emitter.expiredTicketIds).containsExactlyElementsOf(expiredTickets.map { it.id }) },
            { assertThat(repository.deletedTicketIds).containsExactlyElementsOf(expiredTickets.map { it.id }) }
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
    ): RecordingEnrollmentTicketRepository {
        return RecordingEnrollmentTicketRepository(waiting, expired)
    }

    private class RecordingWaitingQueueEmitter : WaitingQueueEmitter {
        val allowedTicketIds = mutableListOf<Long>()
        val expiredTicketIds = mutableListOf<Long>()

        override fun save(ticketId: Long, emitter: org.springframework.web.servlet.mvc.method.annotation.SseEmitter) = Unit

        override fun sendAllowed(ticketId: Long) {
            allowedTicketIds += ticketId
        }

        override fun sendExpired(ticketId: Long) {
            expiredTicketIds += ticketId
        }

        override fun complete(ticketId: Long) = Unit
    }

    private class RecordingEnrollmentTicketRepository(
        private val waitingTickets: List<EnrollmentTicket>,
        private val expiredTickets: List<EnrollmentTicket>
    ) {
        val deletedTicketIds = mutableListOf<Long>()

        val proxy: EnrollmentTicketRepository = Proxy.newProxyInstance(
            EnrollmentTicketRepository::class.java.classLoader,
            arrayOf(EnrollmentTicketRepository::class.java)
        ) { _, method, args ->
            when (method.name) {
                "findAllByStatus" -> {
                    val status = args[0] as EnrollmentTicketStatus
                    val pageable = args[1] as Pageable
                    pageOf(status, pageable)
                }

                "deleteAll" -> {
                    val tickets = args[0] as Iterable<*>
                    deletedTicketIds += tickets.filterIsInstance<EnrollmentTicket>().map { it.id }
                    Unit
                }

                else -> throw UnsupportedOperationException("Unsupported method: ${method.name}")
            }
        } as EnrollmentTicketRepository

        private fun pageOf(status: EnrollmentTicketStatus, pageable: Pageable): Page<EnrollmentTicket> {
            val source = when (status) {
                EnrollmentTicketStatus.WAITING -> waitingTickets
                EnrollmentTicketStatus.EXPIRED -> expiredTickets
                else -> emptyList()
            }
            return PageImpl(source.take(pageable.pageSize), pageable, source.size.toLong())
        }
    }
}
