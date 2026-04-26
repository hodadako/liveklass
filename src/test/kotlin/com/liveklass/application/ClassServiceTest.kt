package com.liveklass.application

import com.liveklass.IntegrationTestSupport
import com.liveklass.domain.Class
import com.liveklass.domain.ClassStatus
import com.liveklass.fixture.DomainTestFixture.getClassFixture
import com.liveklass.fixture.ServiceTestFixture.getCreateClassRequestFixture
import com.liveklass.persistence.ClassRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.ThrowableAssert.catchThrowable
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull

@DisplayName("강의 서비스 통합 테스트")
class ClassServiceTest @Autowired constructor(
    private val sut: ClassUseCase,
    private val classRepository: ClassRepository
) : IntegrationTestSupport() {
    @Test
    fun `강의를 생성한다`() {
        val request = getCreateClassRequestFixture()

        val classId = sut.createClass(request)

        val foundClass = findClass(classId)

        assertAll(
            { assertThat(foundClass.title).isEqualTo(request.title) },
            { assertThat(foundClass.description).isEqualTo(request.description) },
            { assertThat(foundClass.price).isEqualTo(request.price) },
            { assertThat(foundClass.capacity).isEqualTo(request.capacity) },
            { assertThat(foundClass.startDate).isEqualTo(request.startDate) },
            { assertThat(foundClass.endDate).isEqualTo(request.endDate) },
            { assertThat(foundClass.id).isEqualTo(classId) }
        )
    }

    @Test
    fun `강의를 모집 중으로 변경한다`() {
        val savedClass = classRepository.save(getClassFixture())

        sut.openClass(savedClass.id)

        val foundClass = findClass(savedClass.id)

        assertThat(foundClass.classStatus).isEqualTo(ClassStatus.OPEN)
    }

    @Test
    fun `존재하지 않는 강의를 모집 중으로 변경하면 예외가 발생한다`() {
        val exception = catchThrowable { sut.openClass(1L) }

        assertThat(exception).isInstanceOf(EntityNotFoundException::class.java)
    }

    @Test
    fun `강의를 모집 마감으로 변경한다`() {
        val savedClass = classRepository.save(getClassFixture().apply { open() })

        sut.closeClass(savedClass.id)

        val foundClass = findClass(savedClass.id)

        assertThat(foundClass.classStatus).isEqualTo(ClassStatus.CLOSED)
    }

    @Test
    fun `존재하지 않는 강의를 모집 마감으로 변경하면 예외가 발생한다`() {
        val exception = catchThrowable { sut.closeClass(1L) }

        assertThat(exception).isInstanceOf(EntityNotFoundException::class.java)
    }

    @Test
    fun `강의 상세를 조회한다`() {
        val savedClass = classRepository.save(getClassFixture().apply { open() })

        val response = sut.findClassDetail(savedClass.id)

        assertAll(
            { assertThat(response.classId).isEqualTo(savedClass.id) },
            { assertThat(response.title).isEqualTo(savedClass.title) },
            { assertThat(response.description).isEqualTo(savedClass.description) },
            { assertThat(response.classStatus).isEqualTo(savedClass.classStatus) },
            { assertThat(response.price).isEqualTo(savedClass.price) },
            { assertThat(response.enrolledCount).isEqualTo(savedClass.enrolledCount) },
            { assertThat(response.startDate).isEqualTo(savedClass.startDate) },
            { assertThat(response.endDate).isEqualTo(savedClass.endDate) }
        )
    }

    @Test
    fun `존재하지 않는 강의 상세를 조회하면 예외가 발생한다`() {
        val exception = catchThrowable { sut.findClassDetail(1L) }

        assertThat(exception).isInstanceOf(EntityNotFoundException::class.java)
    }

    @Test
    fun `강의 목록을 전체 조회한다`() {
        val draftClass = getClassFixture()
        val closedClass = getClassFixture().apply {
            open()
            close()
        }
        val openClass = getClassFixture().apply { open() }
        val savedClasses = classRepository.saveAll(listOf(draftClass, closedClass, openClass))

        val response = sut.findClasses(null)

        assertAll(
            { assertThat(response.responses).hasSize(3) },
            {
                assertThat(response.responses.map { it.classId })
                    .containsExactlyInAnyOrderElementsOf(savedClasses.map { it.id })
            }
        )
    }

    @Test
    fun `강의 목록을 상태별로 조회한다`() {
        val draftClass = getClassFixture()
        val closedClass = getClassFixture().apply {
            open()
            close()
        }
        val openClass = getClassFixture().apply { open() }
        val savedClasses = classRepository.saveAll(listOf(draftClass, closedClass, openClass))

        val response = sut.findClasses(ClassStatus.OPEN)

        assertAll(
            { assertThat(response.responses).hasSize(1) },
            { assertThat(response.responses).allMatch { it.classStatus == ClassStatus.OPEN } },
            { assertThat(response.responses[0].classId).isEqualTo(savedClasses[2].id) }
        )
    }

    private fun findClass(classId: Long): Class =
        classRepository.findByIdOrNull(classId) ?: error("강의를 찾을 수 없습니다. classId=$classId")
}
