package com.liveklass.domain

import com.liveklass.fixture.DomainTestFixture.getClassFixture
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.ThrowableAssert.catchThrowable
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertAll
import kotlin.test.Test

@DisplayName("강의 도메인 단위 테스트")
class ClassTest {
    private lateinit var sut: Class

    @BeforeEach
    fun setUp() {
        sut = getClassFixture(1L)
    }

    @Test
    fun `강의가 생성되면 기본 상태는 초안이고 수강 신청 횟수는 0회이다`() {
        assertAll(
            { assertThat(sut.classStatus).isEqualTo(ClassStatus.DRAFT) },
            { assertThat(sut.enrolledCount).isEqualTo(0) }
        )
    }

    @Test
    fun `초안인 강의만 모집 중으로 변경할 수 있다`() {
        sut.open()

        assertThat(sut.classStatus).isEqualTo(ClassStatus.OPEN)
    }

    @Test
    fun `이미 모집 중인 강의는 모집 중으로 변경할 수 없다`() {
        sut.open()

        val exception = catchThrowable { sut.open() }

        assertAll(
            { assertThat(exception).isInstanceOf(ClassStatusException::class.java) },
            { assertThat(sut.classStatus).isEqualTo(ClassStatus.OPEN) }
        )
    }

    @Test
    fun `모집 마감인 강의는 모집 중으로 변경할 수 없다`() {
        sut.open()
        sut.close()

        val exception = catchThrowable { sut.open() }

        assertAll(
            { assertThat(exception).isInstanceOf(ClassStatusException::class.java) },
            { assertThat(sut.classStatus).isEqualTo(ClassStatus.CLOSED) }
        )
    }

    @Test
    fun `모집 중인 강의는 모집 마감으로 변경할 수 있다`() {
        sut.open()
        sut.close()

        assertThat(sut.classStatus).isEqualTo(ClassStatus.CLOSED)
    }

    @Test
    fun `초안인 강의는 모집 마감로 변경할 수 없다`() {
        val exception = catchThrowable { sut.close() }

        assertAll(
            { assertThat(exception).isInstanceOf(ClassStatusException::class.java) },
            { assertThat(sut.classStatus).isEqualTo(ClassStatus.DRAFT) }
        )
    }

    @Test
    fun `모집 중인 강의는 수강 확정할 수 있고 확정 인원이 1 증가한다`() {
        sut.open()

        sut.confirmEnrollment()

        assertThat(sut.enrolledCount).isEqualTo(1)
    }

    @Test
    fun `초안인 강의는 수강 요청할 수 없다`() {
        val exception = catchThrowable {
            sut.validateEnrollmentRequest()
        }

        assertAll(
            { assertThat(exception).isInstanceOf(ClassEnrollmentException::class.java) },
            { assertThat(sut.classStatus).isEqualTo(ClassStatus.DRAFT) },
            { assertThat(sut.enrolledCount).isEqualTo(0) }
        )
    }

    @Test
    fun `모집 마감된 강의는 수강 요청할 수 없다`() {
        sut.open()
        sut.close()

        val exception = catchThrowable {
            sut.validateEnrollmentRequest()
        }

        assertAll(
            { assertThat(exception).isInstanceOf(ClassEnrollmentException::class.java) },
            { assertThat(sut.classStatus).isEqualTo(ClassStatus.CLOSED) },
            { assertThat(sut.enrolledCount).isEqualTo(0) }
        )
    }

    @Test
    fun `정원이 가득 찬 강의는 수강 확정할 수 없다`() {
        sut.open()
        sut.confirmEnrollment()

        val exception = catchThrowable {
            sut.confirmEnrollment()
        }

        assertAll(
            { assertThat(exception).isInstanceOf(ClassCapacityExceededException::class.java) },
            { assertThat(sut.classStatus).isEqualTo(ClassStatus.OPEN) },
            { assertThat(sut.enrolledCount).isEqualTo(1) }
        )
    }

    @Test
    fun `확정된 수강 인원은 취소 시 1 감소한다`() {
        sut.open()
        sut.confirmEnrollment()

        sut.cancelEnrollment()

        assertThat(sut.enrolledCount).isEqualTo(0)
    }

    @Test
    fun `확정된 수강 인원이 없으면 취소할 수 없다`() {
        val exception = catchThrowable { sut.cancelEnrollment() }

        assertAll(
            { assertThat(exception).isInstanceOf(ClassEnrollmentCancelException::class.java) },
            { assertThat(sut.enrolledCount).isEqualTo(0) }
        )
    }
}
