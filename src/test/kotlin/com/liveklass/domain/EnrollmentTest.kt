package com.liveklass.domain

import com.liveklass.fixture.DomainTestFixture.getTestClass
import com.liveklass.fixture.DomainTestFixture.getTestMember
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.ThrowableAssert.catchThrowable
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import java.time.LocalDateTime

@DisplayName("수강 신청 도메인 단위 테스트")
class EnrollmentTest {
    private lateinit var sut: Enrollment
    private lateinit var enrolledClass: Class
    private lateinit var student: Member
    private lateinit var now: LocalDateTime

    @BeforeEach
    fun setUp() {
        enrolledClass = getTestClass()
        student = getTestMember()
        now = LocalDateTime.of(2026, 4, 4, 12, 10)
        sut = Enrollment.create(enrolledClass, student, now)
    }

    @Test
    fun `대기 상태인 수강 신청만 확정할 수 있다`() {
        sut.confirm(now)

        assertAll(
            { assertThat(sut.enrollmentStatus).isEqualTo(EnrollmentStatus.CONFIRMED) },
            { assertThat(sut.confirmedDate).isEqualTo(now) }
        )
    }

    @Test
    fun `대기 상태인 수강 신청은 취소할 수 있다`() {
        sut.cancel(now)

        assertAll(
            { assertThat(sut.enrollmentStatus).isEqualTo(EnrollmentStatus.CANCELLED) },
            { assertThat(sut.cancelledDate).isEqualTo(now) }
        )
    }

    @Test
    fun `확정된 수강 신청은 취소할 수 있다`() {
        sut.confirm(now)
        sut.cancel(now)

        assertAll(
            { assertThat(sut.enrollmentStatus).isEqualTo(EnrollmentStatus.CANCELLED) },
            { assertThat(sut.cancelledDate).isEqualTo(now) }
        )
    }

    @Test
    fun `수강 신청 후 정확히 7일째에는 취소할 수 있다`() {
        val requestedDate = LocalDateTime.of(2026, 4, 1, 12, 0)
        val cancelDate = requestedDate.plusDays(7)

        sut = Enrollment.create(
            enrolledClass = enrolledClass,
            student = student,
            requestedDate = requestedDate
        )

        sut.cancel(cancelDate)

        assertAll(
            { assertThat(sut.enrollmentStatus).isEqualTo(EnrollmentStatus.CANCELLED) },
            { assertThat(sut.cancelledDate).isEqualTo(cancelDate) }
        )
    }

    @Test
    fun `수강 신청 후 7일이 지나면 취소할 수 없다`() {
        val requestedDate = LocalDateTime.of(2026, 4, 1, 12, 0)
        val cancelDate = requestedDate.plusDays(7).plusNanos(1)

        sut = Enrollment.create(
            enrolledClass = enrolledClass,
            student = student,
            requestedDate = requestedDate
        )

        val exception = catchThrowable { sut.cancel(cancelDate) }

        assertAll(
            { assertThat(exception).isInstanceOf(EnrollmentCancelException::class.java) },
            { assertThat(sut.enrollmentStatus).isEqualTo(EnrollmentStatus.PENDING) },
            { assertThat(sut.cancelledDate).isNull() }
        )
    }

    @Test
    fun `확정된 수강 신청을 다시 확정할 수 없다`() {
        sut.confirm(now)

        val confirmedDate = LocalDateTime.of(2026, 4, 4, 12, 15)

        val exception = catchThrowable { sut.confirm(confirmedDate) }
        assertAll(
            { assertThat(exception).isInstanceOf(EnrollmentStatusException::class.java) },
            { assertThat(sut.enrollmentStatus).isEqualTo(EnrollmentStatus.CONFIRMED) },
            { assertThat(sut.confirmedDate).isEqualTo(now) }
        )
    }

    @Test
    fun `확정 후 취소된 수강 신청을 다시 확정할 수 없다`() {
        sut.confirm(now)
        sut.cancel(now)

        val exception = catchThrowable { sut.confirm(now) }
        assertAll(
            { assertThat(exception).isInstanceOf(EnrollmentStatusException::class.java) },
            { assertThat(sut.enrollmentStatus).isEqualTo(EnrollmentStatus.CANCELLED) }
        )
    }

    @Test
    fun `취소된 수강 신청을 다시 취소할 수 없다`() {
        sut.cancel(now)

        val cancelDate = now.plusMinutes(5)

        val exception = catchThrowable { sut.cancel(cancelDate) }

        assertAll(
            { assertThat(exception).isInstanceOf(EnrollmentStatusException::class.java) },
            { assertThat(sut.enrollmentStatus).isEqualTo(EnrollmentStatus.CANCELLED) },
            { assertThat(sut.cancelledDate).isEqualTo(now) }
        )
    }

    @Test
    fun `확정된 수강 신청도 7일이 지나면 취소할 수 없다`() {
        val requestedDate = LocalDateTime.of(2026, 4, 1, 12, 0)
        val confirmedDate = requestedDate.plusDays(1)
        val cancelDate = requestedDate.plusDays(7).plusNanos(1)

        sut = Enrollment.create(
            enrolledClass = enrolledClass,
            student = student,
            requestedDate = requestedDate
        )
        sut.confirm(confirmedDate)

        val exception = catchThrowable { sut.cancel(cancelDate) }

        assertAll(
            { assertThat(exception).isInstanceOf(EnrollmentCancelException::class.java) },
            { assertThat(sut.enrollmentStatus).isEqualTo(EnrollmentStatus.CONFIRMED) },
            { assertThat(sut.confirmedDate).isEqualTo(confirmedDate) },
            { assertThat(sut.cancelledDate).isNull() }
        )
    }
}
