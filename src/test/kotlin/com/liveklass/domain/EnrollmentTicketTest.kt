package com.liveklass.domain

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.ThrowableAssert.catchThrowable
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import java.time.LocalDateTime

@DisplayName("수강 대기열 티켓 도메인 단위 테스트")
class EnrollmentTicketTest {
    private lateinit var sut: EnrollmentTicket
    private lateinit var now: LocalDateTime

    @BeforeEach
    fun setUp() {
        now = LocalDateTime.of(2026, 4, 4, 12, 10)
        sut = EnrollmentTicket.create(1L, 2L)
    }

    @Test
    fun `티켓이 생성되면 기본 상태는 대기이고 시간 정보가 비어 있다`() {
        assertAll(
            { assertThat(sut.status).isEqualTo(EnrollmentTicketStatus.WAITING) },
            { assertThat(sut.allowedAt).isNull() },
            { assertThat(sut.expiresAt).isNull() },
            { assertThat(sut.usedAt).isNull() }
        )
    }

    @Test
    fun `허용된 티켓은 사용할 수 있다`() {
        sut.allow(now, 5)
        val usedAt = now.plusMinutes(1)
        sut.use(usedAt)

        assertAll(
            { assertThat(sut.status).isEqualTo(EnrollmentTicketStatus.USED) },
            { assertThat(sut.usedAt).isEqualTo(usedAt) }
        )
    }

    @Test
    fun `대기중인 티켓은 허용할 수 있다`() {
        sut.allow(now, 5)

        assertAll(
            { assertThat(sut.status).isEqualTo(EnrollmentTicketStatus.ALLOWED) },
            { assertThat(sut.allowedAt).isEqualTo(now) },
            { assertThat(sut.expiresAt).isEqualTo(now.plusMinutes(5)) },
            { assertThat(sut.usedAt).isNull() }
        )
    }

    @Test
    fun `만료 시간이 지난 허용 티켓은 사용 시 만료된다`() {
        sut.allow(now, 5)
        val expiredAt = now.plusMinutes(6)

        val exception = catchThrowable { sut.use(expiredAt) }

        assertAll(
            { assertThat(sut.status).isEqualTo(EnrollmentTicketStatus.EXPIRED) },
            { assertThat(exception).isInstanceOf(EnrollmentTicketExpiredException::class.java) },
            { assertThat(sut.usedAt).isNull() }
        )
    }

    @Test
    fun `허용 상태가 아닌 수강권은 사용할 수 없다`() {
        val usedAt = now.minusMinutes(1)
        sut.usedAt = usedAt

        val exception = catchThrowable { sut.use(now) }

        assertAll(
            { assertThat(exception).isInstanceOf(EnrollmentTicketStatusException::class.java) },
            { assertThat(sut.status).isEqualTo(EnrollmentTicketStatus.WAITING) },
            { assertThat(sut.usedAt).isEqualTo(usedAt) }
        )
    }

    @Test
    fun `대기 상태가 아닌 수강권은 허용할 수 없다`() {
        sut.allow(now, 5)
        val allowedAt = sut.allowedAt
        val expiresAt = sut.expiresAt

        val exception = catchThrowable { sut.allow(now, 5) }

        assertAll(
            { assertThat(exception).isInstanceOf(EnrollmentTicketStatusException::class.java) },
            { assertThat(sut.status).isEqualTo(EnrollmentTicketStatus.ALLOWED) },
            { assertThat(sut.allowedAt).isEqualTo(allowedAt) },
            { assertThat(sut.expiresAt).isEqualTo(expiresAt) }
        )
    }

    @Test
    fun `만료 시간이 지난 허용 티켓은 재사용할 수 없다`() {
        sut.allow(now, 5)
        val firstAttemptAt = now.plusMinutes(6)
        catchThrowable { sut.use(firstAttemptAt) }

        val secondAttemptAt = now.plusMinutes(7)

        val exception = catchThrowable { sut.use(secondAttemptAt) }

        assertAll(
            { assertThat(exception).isInstanceOf(EnrollmentTicketStatusException::class.java) },
            { assertThat(sut.status).isEqualTo(EnrollmentTicketStatus.EXPIRED) },
            { assertThat(sut.allowedAt).isEqualTo(now) },
            { assertThat(sut.expiresAt).isEqualTo(now.plusMinutes(5)) },
            { assertThat(sut.usedAt).isNull() }
        )
    }
}
