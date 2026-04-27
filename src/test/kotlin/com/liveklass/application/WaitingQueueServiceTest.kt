package com.liveklass.application

import com.liveklass.IntegrationTestSupport
import com.liveklass.domain.EnrollmentTicketStatus
import com.liveklass.fixture.DomainTestFixture.getEnrollmentTicketFixture
import com.liveklass.persistence.EnrollmentTicketRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import java.time.LocalDateTime

@DisplayName("대기열 서비스 통합 테스트")
class WaitingQueueServiceTest @Autowired constructor(
    private val sut: WaitingQueueUseCase,
    private val enrollmentTicketRepository: EnrollmentTicketRepository
) : IntegrationTestSupport() {

    @Test
    fun `대기 티켓 조회 시 선착순 최대 10개 티켓을 반환하고 허용 상태로 변경한다`() {
        val waitingTickets = (1L..11L).map { memberId ->
            enrollmentTicketRepository.save(getEnrollmentTicketFixture(1L, memberId))
        }
        enrollmentTicketRepository.save(
            getEnrollmentTicketFixture(1L, 100L).apply {
                allow(LocalDateTime.of(2026, 4, 4, 12, 10), 5)
            }
        )

        val ticketIds = sut.findWaitingTickets()

        val processedTickets = waitingTickets.take(QUEUE_PAGE_SIZE).map { findTicket(it.id) }
        val untouchedTicket = findTicket(waitingTickets.last().id)
        assertAll(
            { assertThat(ticketIds).containsExactlyElementsOf(waitingTickets.take(QUEUE_PAGE_SIZE).map { it.id }) },
            { assertThat(processedTickets).allMatch { it.status == EnrollmentTicketStatus.ALLOWED } },
            { assertThat(untouchedTicket.status).isEqualTo(EnrollmentTicketStatus.WAITING) }
        )
    }

    @Test
    fun `만료된 티켓 삭제 시 만료 티켓 아이디만 반환하고 실제로 삭제한다`() {
        val baseTime = LocalDateTime.of(2026, 4, 4, 12, 10)
        val expiredTicket1 = enrollmentTicketRepository.save(
            getEnrollmentTicketFixture(1L, 1L).apply {
                allow(baseTime, 5)
                runCatching { use(baseTime.plusMinutes(6)) }
            }
        )
        val expiredTicket2 = enrollmentTicketRepository.save(
            getEnrollmentTicketFixture(1L, 2L).apply {
                allow(baseTime, 5)
                runCatching { use(baseTime.plusMinutes(6)) }
            }
        )
        val waitingTicket = enrollmentTicketRepository.save(getEnrollmentTicketFixture(1L, 3L))

        val deletedTicketIds = sut.deleteExpiredTickets()

        assertAll(
            { assertThat(deletedTicketIds).containsExactly(expiredTicket1.id, expiredTicket2.id) },
            { assertThat(enrollmentTicketRepository.findByIdOrNull(expiredTicket1.id)).isNull() },
            { assertThat(enrollmentTicketRepository.findByIdOrNull(expiredTicket2.id)).isNull() },
            { assertThat(findTicket(waitingTicket.id).status).isEqualTo(EnrollmentTicketStatus.WAITING) }
        )
    }

    private fun findTicket(ticketId: Long) =
        enrollmentTicketRepository.findByIdOrThrow(ticketId)
}
