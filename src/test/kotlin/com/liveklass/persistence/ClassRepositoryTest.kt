package com.liveklass.persistence

import com.liveklass.IntegrationTestSupport
import com.liveklass.domain.ClassStatus
import com.liveklass.fixture.DomainTestFixture.getClassFixture
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Named.named
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import java.util.stream.Stream

@DisplayName("강의 Repository 통합 테스트")
class ClassRepositoryTest @Autowired constructor(
    private val sut: ClassRepository
) : IntegrationTestSupport() {

    @Test
    fun `강의를 저장하면 Auditing 필드가 기록되고 id가 생성된다`() {
        val klass = getClassFixture()

        val savedClass = sut.save(klass)

        assertAll(
            { assertThat(savedClass.id).isNotZero() },
            { assertThat(savedClass.createdDate).isNotNull() },
            { assertThat(savedClass.updatedDate).isNotNull() }
        )
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("classStatusProvider")
    fun `특정 강의 상태로 조회시 해당하는 강의 목록을 반환한다`(
        classStatus: ClassStatus?,
        totalClass: Int
    ) {
        val testClass1 = getClassFixture()
        val testClass2 = getClassFixture().apply {
            open()
            close()
        }
        val testClass3 = getClassFixture().apply {
            open()
        }
        sut.saveAll(listOf(testClass1, testClass2, testClass3))

        val foundClasses = sut.findAllByClassStatus(classStatus)

        assertAll(
            { assertThat(foundClasses.size).isEqualTo(totalClass) },
            { assertThat(foundClasses[0].id).isNotZero() },
            {
                if (classStatus != null) {
                    assertThat(foundClasses)
                        .allSatisfy { assertThat(it.classStatus).isEqualTo(classStatus) }
                }
            }
        )
    }

    @Test
    @Transactional
    fun `비관적 락을 통해서 강의를 조회할 수 있다`() {
        val savedClass = sut.save(getClassFixture())

        val foundClass = sut.findByIdForUpdate(savedClass.id)

        assertAll(
            { assertThat(foundClass).isNotNull },
            { assertThat(foundClass!!.id).isEqualTo(savedClass.id) }
        )
    }

    companion object {
        @JvmStatic
        fun classStatusProvider(): Stream<Arguments> =
            Stream.of(
                Arguments.of(named("초안일 때", ClassStatus.DRAFT), 1),
                Arguments.of(named("모집 중일 때", ClassStatus.OPEN), 1),
                Arguments.of(named("모집 완료되었을 때", ClassStatus.CLOSED), 1),
                Arguments.of(named("모든 강의 조회 시", null), 3)
            )
    }
}
