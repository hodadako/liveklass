package com.liveklass.persistence

import com.liveklass.IntegrationTestSupport
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
    fun `강의를 저장하면 Auditing 필드가 기록된다`() {
        val klass = getClassFixture()

        val savedClass = classRepository.saveAndFlush(klass)

        assertAll(
            { assertThat(savedClass.id).isNotZero() },
            { assertThat(savedClass.createdDate).isNotNull() },
            { assertThat(savedClass.updatedDate).isNotNull() }
        )
    }

    @Test
    @Transactional
    fun `비관적 락으로 강의를 조회할 수 있다`() {
        val savedClass = classRepository.saveAndFlush(getClassFixture())

        val foundClass = classRepository.findByIdForUpdate(savedClass.id)

        assertAll(
            { assertThat(foundClass).isNotNull },
            { assertThat(foundClass!!.id).isEqualTo(savedClass.id) }
        )
    }
}
