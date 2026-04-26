package com.liveklass.persistence

import com.liveklass.IntegrationTestSupport
import com.liveklass.domain.ClassStatus
import com.liveklass.fixture.DomainTestFixture.getClassFixture
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

@DisplayName("강의 Repository 통합 테스트")
class ClassRepositoryTest : IntegrationTestSupport() {

    @Autowired
    private lateinit var classRepository: ClassRepository

    @Test
    fun `강의를 저장하면 Auditing 필드가 기록되고 id가 생성된다`() {
        val klass = getClassFixture()

        val savedClass = classRepository.saveAndFlush(klass)

        assertAll(
            { assertThat(savedClass.id).isNotZero() },
            { assertThat(savedClass.createdDate).isNotNull() },
            { assertThat(savedClass.updatedDate).isNotNull() }
        )
    }

    @Test
    fun `특정 강의 상태로 조회시 해당하는 강의 목록을 반환한다`() {
        val testClass1 = getClassFixture()
        val testClass2 = getClassFixture()
        testClass2.open()

        classRepository.save(testClass1)
        classRepository.save(testClass2)

        val foundClasses = classRepository.findAllByClassStatus(ClassStatus.DRAFT)

        assertAll(
            { assertThat(foundClasses.size).isEqualTo(1) },
            { assertThat(foundClasses[0].id).isNotZero() },
            { assertThat(foundClasses[0].classStatus).isEqualTo(ClassStatus.DRAFT) }
        )
    }

    @Test
    @Transactional
    fun `비관적 락을 통해서 강의를 조회할 수 있다`() {
        val savedClass = classRepository.saveAndFlush(getClassFixture())

        val foundClass = classRepository.findByIdForUpdate(savedClass.id)

        assertAll(
            { assertThat(foundClass).isNotNull },
            { assertThat(foundClass!!.id).isEqualTo(savedClass.id) }
        )
    }
}
