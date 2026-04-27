package com.liveklass.application

import com.liveklass.IntegrationTestSupport
import com.liveklass.domain.EnrollmentTicketStatus
import com.liveklass.fixture.DomainTestFixture.getEnrollmentTicketFixture
import com.liveklass.persistence.EnrollmentTicketRepository
import com.liveklass.runConcurrently
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.ThrowableAssert.catchThrowable
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import java.time.LocalDateTime

@DisplayName("수강 대기열 티켓 서비스 통합 테스트")
class EnrollmentTicketServiceTest @Autowired constructor(
    private val sut: EnrollmentTicketUseCase,
    private val enrollmentTicketRepository: EnrollmentTicketRepository
) : IntegrationTestSupport() {

    @Test
    fun `대기열에 진입하면 티켓을 생성하고 현재 대기 정보를 반환한다`() {
        val response = sut.enterWaitingQueue(1L, 1L)

        val foundTicket = findTicket(response.ticketId)

        assertAll(
            { assertThat(foundTicket.classId).isEqualTo(1L) },
            { assertThat(foundTicket.memberId).isEqualTo(1L) },
            { assertThat(foundTicket.status).isEqualTo(EnrollmentTicketStatus.WAITING) },
            { assertThat(response.ticketId).isEqualTo(foundTicket.id) },
            { assertThat(response.position).isEqualTo(0L) },
            { assertThat(response.waitingCount).isEqualTo(1L) },
            { assertThat(response.estimatedWaitingTime).isEqualTo(0L) }
        )
    }

    @Test
    fun `같은 강의와 회원으로 다시 진입하면 기존 티켓을 재사용한다`() {
        val savedTicket = enrollmentTicketRepository.saveAndFlush(getEnrollmentTicketFixture(1L, 1L))

        val response = sut.enterWaitingQueue(1L, 1L)

        assertAll(
            { assertThat(response.ticketId).isEqualTo(savedTicket.id) },
            { assertThat(response.position).isEqualTo(0L) },
            { assertThat(response.waitingCount).isEqualTo(1L) },
            { assertThat(enrollmentTicketRepository.count()).isEqualTo(1L) }
        )
    }

    @Test
    fun `같은 강의와 회원이 동시에 대기열에 진입하면 티켓은 하나만 생성된다`() {
        val results = runConcurrently(
            { sut.enterWaitingQueue(1L, 1L) },
            { sut.enterWaitingQueue(1L, 1L) }
        )

        val successCount = results.count { it.isSuccess }
        val failures = results.mapNotNull { it.exceptionOrNull() }

        assertAll(
            { assertThat(successCount).isEqualTo(1) },
            { assertThat(failures).hasSize(1) },
            { assertThat(failures[0]).isInstanceOf(DataIntegrityViolationException::class.java) },
            { assertThat(enrollmentTicketRepository.count()).isEqualTo(1L) },
            { assertThat(enrollmentTicketRepository.findByClassIdAndMemberId(1L, 1L)).isNotNull() }
        )
    }

    @Test
    fun `현재 대기 정보는 다른 강의의 대기 티켓도 포함한 전체 순서를 기준으로 계산한다`() {
        val baseTime = LocalDateTime.of(2026, 4, 4, 12, 10)
        enrollmentTicketRepository.save(getEnrollmentTicketFixture(100L, 10L))
        enrollmentTicketRepository.save(getEnrollmentTicketFixture(200L, 20L))
        val savedTicket = enrollmentTicketRepository.save(getEnrollmentTicketFixture(300L, 30L))
        enrollmentTicketRepository.save(
            getEnrollmentTicketFixture(400L, 40L).apply {
                allow(baseTime, 5)
            }
        )

        val response = sut.findCurrentWaiting(savedTicket.id)

        assertAll(
            { assertThat(response.ticketId).isEqualTo(savedTicket.id) },
            { assertThat(response.position).isEqualTo(2L) },
            { assertThat(response.waitingCount).isEqualTo(3L) },
            { assertThat(response.estimatedWaitingTime).isEqualTo(1L) }
        )
    }

    @Test
    fun `존재하지 않는 티켓의 현재 대기 정보를 조회하면 예외가 발생한다`() {
        val exception = catchThrowable { sut.findCurrentWaiting(1L) }

        assertThat(exception).isInstanceOf(EntityNotFoundException::class.java)
    }

    private fun findTicket(ticketId: Long) =
        enrollmentTicketRepository.findByIdOrThrow(ticketId)
}
