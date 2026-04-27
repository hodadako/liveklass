package com.liveklass.persistence

import com.liveklass.IntegrationTestSupport
import com.liveklass.domain.EnrollmentTicketStatus
import com.liveklass.fixture.DomainTestFixture.getEnrollmentTicketFixture
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@DisplayName("수강 대기열 티켓 Repository 통합 테스트")
class EnrollmentTicketRepositoryTest @Autowired constructor(
    private val sut: EnrollmentTicketRepository
) : IntegrationTestSupport() {

    @Test
    fun `수강 대기열 티켓을 저장하면 Auditing 필드가 기록되고 id가 생성된다`() {
        val ticket = getEnrollmentTicketFixture()

        val savedTicket = sut.save(ticket)

        assertAll(
            { assertThat(savedTicket.id).isNotZero() },
            { assertThat(savedTicket.createdDate).isNotNull() },
            { assertThat(savedTicket.updatedDate).isNotNull() }
        )
    }

    @Test
    @Transactional
    fun `강의 아이디와 회원 아이디로 수강 대기열 티켓을 조회할 수 있다`() {
        val savedTicket = sut.save(getEnrollmentTicketFixture(10L, 20L))
        sut.save(getEnrollmentTicketFixture(10L, 21L))

        val foundTicket = sut.findByClassIdAndMemberId(10L, 20L)

        assertAll(
            { assertThat(foundTicket!!.id).isEqualTo(savedTicket.id) },
            { assertThat(foundTicket!!.classId).isEqualTo(10L) },
            { assertThat(foundTicket!!.memberId).isEqualTo(20L) }
        )
    }

    @Test
    fun `상태별로 수강 대기열 티켓 수를 셀 수 있다`() {
        val baseTime = LocalDateTime.of(2026, 4, 4, 12, 10)
        val waitingTicket1 = getEnrollmentTicketFixture(1L, 1L)
        val waitingTicket2 = getEnrollmentTicketFixture(1L, 2L)
        val allowedTicket = getEnrollmentTicketFixture(1L, 3L).apply { allow(baseTime, 5) }
        val usedTicket = getEnrollmentTicketFixture(1L, 4L).apply {
            allow(baseTime, 5)
            use(baseTime.plusMinutes(1))
        }
        val expiredTicket = getEnrollmentTicketFixture(1L, 5L).apply {
            allow(baseTime, 5)
            runCatching { use(baseTime.plusMinutes(6)) }
        }
        sut.saveAll(listOf(waitingTicket1, waitingTicket2, allowedTicket, usedTicket, expiredTicket))

        assertAll(
            { assertThat(sut.countByStatus(EnrollmentTicketStatus.WAITING)).isEqualTo(2L) },
            { assertThat(sut.countByStatus(EnrollmentTicketStatus.ALLOWED)).isEqualTo(1L) },
            { assertThat(sut.countByStatus(EnrollmentTicketStatus.USED)).isEqualTo(1L) },
            { assertThat(sut.countByStatus(EnrollmentTicketStatus.EXPIRED)).isEqualTo(1L) }
        )
    }

    @Test
    fun `현재 순서는 대기 상태이면서 더 작은 아이디를 가진 티켓 수로 계산한다`() {
        val baseTime = LocalDateTime.of(2026, 4, 4, 12, 10)
        sut.save(getEnrollmentTicketFixture(1L, 1L))
        sut.save(getEnrollmentTicketFixture(1L, 2L))
        sut.save(getEnrollmentTicketFixture(1L, 3L).apply { allow(baseTime, 5) })
        val targetTicket = sut.save(getEnrollmentTicketFixture(1L, 4L))

        val position = sut.findCurrentPosition(targetTicket.id, EnrollmentTicketStatus.WAITING)

        assertThat(position).isEqualTo(2L)
    }

    @Test
    fun `대기 중인 수강 대기열 티켓만 아이디 순으로 페이징 조회할 수 있다`() {
        val baseTime = LocalDateTime.of(2026, 4, 4, 12, 10)
        val waitingTicket1 = sut.save(getEnrollmentTicketFixture(1L, 1L))
        val waitingTicket2 = sut.save(getEnrollmentTicketFixture(2L, 2L))
        sut.save(getEnrollmentTicketFixture(3L, 3L).apply { allow(baseTime, 5) })
        val waitingTicket3 = sut.save(getEnrollmentTicketFixture(4L, 4L))
        sut.save(
            getEnrollmentTicketFixture(5L, 5L).apply {
                allow(baseTime, 5)
                use(baseTime.plusMinutes(1))
            }
        )
        val pageable = PageRequest.of(0, 2)

        val page = sut.findAllByStatus(EnrollmentTicketStatus.WAITING, pageable)

        assertAll(
            { assertThat(page.content).hasSize(2) },
            { assertThat(page.totalElements).isEqualTo(3L) },
            { assertThat(page.content).allMatch { it.status == EnrollmentTicketStatus.WAITING } },
            { assertThat(page.content.map { it.id }).containsExactly(waitingTicket1.id, waitingTicket2.id) },
            { assertThat(page.content.map { it.id }).doesNotContain(waitingTicket3.id) }
        )
    }
}
