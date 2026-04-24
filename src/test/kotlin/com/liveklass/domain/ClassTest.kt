package com.liveklass.domain

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.ThrowableAssert.catchThrowable
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertAll
import java.time.LocalDateTime
import kotlin.test.Test

@DisplayName("강의 도메인 단위 테스트")
class ClassTest {
    private lateinit var sut: Class

    @BeforeEach
    fun setUp() {
        sut = Class.create(
            "쉽게 시작하는 Kotlin",
            "처음 배우는 Kotlin",
            100000,
            1000,
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(1)
        )
    }

    @Test
    fun `강의가 생성되면 기본 상태는 초안(DRAFT)이다`() {
        assertThat(sut.classStatus).isEqualTo(ClassStatus.DRAFT)
    }

    @Test
    fun `초안(DRAFT)인 강의만 모집 중(OPEN)으로 변경할 수 있다`() {
        sut.open()

        assertThat(sut.classStatus).isEqualTo(ClassStatus.OPEN)
    }

    @Test
    fun `이미 모집 중(OPEN)인 강의는 모집 중(OPEN)으로 변경할 수 없다`() {
        sut.open()

        val caughtException = catchThrowable { sut.open() }

        assertAll(
            { assertThat(caughtException).isInstanceOf(ClassStatusException::class.java) },
            { assertThat(sut.classStatus).isEqualTo(ClassStatus.OPEN) }
        )
    }

    @Test
    fun `모집 마감(CLOSED)인 강의는 모집 중(OPEN)으로 변경할 수 없다`() {
        sut.open()
        sut.close()

        val caughtException = catchThrowable { sut.open() }

        assertAll(
            { assertThat(caughtException).isInstanceOf(ClassStatusException::class.java) },
            { assertThat(sut.classStatus).isEqualTo(ClassStatus.CLOSED) }
        )
    }

    @Test
    fun `모집 중(OPEN)인 강의는 모집 마감(CLOSED)으로 변경할 수 있다`() {
        sut.open()
        sut.close()

        assertThat(sut.classStatus).isEqualTo(ClassStatus.CLOSED)
    }

    @Test
    fun `초안(DRAFT)인 강의는 모집 마감(CLOSED)로 변경할 수 없다`() {
        val caughtException = catchThrowable { sut.close() }

        assertAll(
            { assertThat(caughtException).isInstanceOf(ClassStatusException::class.java) },
            { assertThat(sut.classStatus).isEqualTo(ClassStatus.DRAFT) }
        )
    }
}
