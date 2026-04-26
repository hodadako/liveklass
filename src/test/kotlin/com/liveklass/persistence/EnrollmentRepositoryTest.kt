package com.liveklass.persistence

import com.liveklass.IntegrationTestSupport
import com.liveklass.fixture.DomainTestFixture.getClassFixture
import com.liveklass.fixture.DomainTestFixture.getClassListFixture
import com.liveklass.fixture.DomainTestFixture.getEnrollmentFixture
import com.liveklass.fixture.DomainTestFixture.getEnrollmentListFixture
import com.liveklass.fixture.DomainTestFixture.getMemberFixture
import com.liveklass.fixture.DomainTestFixture.getMemberListFixture
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest

@DisplayName("수강 신청 Repository 통합 테스트")
class EnrollmentRepositoryTest : IntegrationTestSupport() {

    @Autowired
    private lateinit var sut: EnrollmentRepository

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Autowired
    private lateinit var classRepository: ClassRepository

    @Test
    fun `수강 신청을 저장하면 Auditing 필드가 기록되고 id가 생성된다`() {
        val member = memberRepository.save(getMemberFixture())
        val klass = classRepository.save(getClassFixture())

        val enrollment = getEnrollmentFixture(klass, member)

        val savedEnrollment = sut.save(enrollment)

        assertAll(
            { assertThat(savedEnrollment.id).isNotZero() },
            { assertThat(savedEnrollment.createdDate).isNotNull() },
            { assertThat(savedEnrollment.updatedDate).isNotNull() }
        )
    }

    @Test
    fun `회원 아이디를 통해서 수강 신청 목록을 페이징 조회할 수 있다`() {
        val members = memberRepository.saveAll(getMemberListFixture())
        val classes = classRepository.saveAll(getClassListFixture())
        sut.saveAll(getEnrollmentListFixture(classes, members))
        val memberId = 1L
        val pageable = PageRequest.of(0, 5)

        val page = sut.findAllByMember(memberId, pageable)

        assertAll(
            { assertThat(page.content).hasSize(5) },
            { assertThat(page.totalElements).isEqualTo(6) },
            { assertThat(page.content).allMatch { it.student.id == memberId } },
            { assertThat(page.content).allMatch { it.enrolledClass.id != 0L } }
        )
    }

    @Test
    fun `강의 아이디를 통해서 수강 신청 목록을 페이징 조회할 수 있다`() {
        val members = memberRepository.saveAll(getMemberListFixture())
        val classes = classRepository.saveAll(getClassListFixture())
        sut.saveAll(getEnrollmentListFixture(classes, members))
        val classId = 1L
        val pageable = PageRequest.of(0, 5)

        val page = sut.findAllByClass(classId, pageable)

        assertAll(
            { assertThat(page.content).hasSize(5) },
            { assertThat(page.totalElements).isEqualTo(6) },
            { assertThat(page.content).allMatch { it.enrolledClass.id == classId } },
            { assertThat(page.content).allMatch { it.student.id != 0L } }
        )
    }
}
