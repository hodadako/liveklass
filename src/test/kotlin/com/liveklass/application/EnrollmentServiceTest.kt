package com.liveklass.application

import com.liveklass.IntegrationTestSupport
import com.liveklass.domain.EnrollmentStatus
import com.liveklass.domain.Member
import com.liveklass.domain.MemberRole
import com.liveklass.fixture.DomainTestFixture.getClassFixture
import com.liveklass.fixture.DomainTestFixture.getEnrollmentFixture
import com.liveklass.persistence.ClassRepository
import com.liveklass.persistence.EnrollmentRepository
import com.liveklass.persistence.MemberRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.ThrowableAssert.catchThrowable
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired

@DisplayName("수강 신청 서비스 통합 테스트")
class EnrollmentServiceTest @Autowired constructor(
    private val sut: EnrollmentUseCase,
    private val enrollmentRepository: EnrollmentRepository,
    private val memberRepository: MemberRepository,
    private val classRepository: ClassRepository
) : IntegrationTestSupport() {

    @Test
    fun `수강 신청을 확정할 수 있다`() {
        val savedEnrollment = saveEnrollment("student")

        sut.completeEnrollment(savedEnrollment.id)

        val foundEnrollment = enrollmentRepository.findByIdOrThrow(savedEnrollment.id)
        val foundClass = classRepository.findByIdOrThrow(savedEnrollment.enrolledClass.id)
        assertAll(
            { assertThat(foundEnrollment.enrollmentStatus).isEqualTo(EnrollmentStatus.CONFIRMED) },
            { assertThat(foundEnrollment.confirmedDate).isNotNull() },
            { assertThat(foundClass.enrolledCount).isEqualTo(1) }
        )
    }

    @Test
    fun `확정된 본인 수강 신청은 취소할 수 있고 확정 인원이 감소한다`() {
        val savedEnrollment = saveEnrollment("student")
        sut.completeEnrollment(savedEnrollment.id)

        sut.cancelEnrollment(savedEnrollment.id, savedEnrollment.student.id)

        val foundEnrollment = enrollmentRepository.findByIdOrThrow(savedEnrollment.id)
        val foundClass = classRepository.findByIdOrThrow(savedEnrollment.enrolledClass.id)
        assertAll(
            { assertThat(foundEnrollment.enrollmentStatus).isEqualTo(EnrollmentStatus.CANCELLED) },
            { assertThat(foundEnrollment.cancelledDate).isNotNull() },
            { assertThat(foundClass.enrolledCount).isEqualTo(0) }
        )
    }

    @Test
    fun `대기 상태인 본인 수강 신청은 취소해도 확정 인원은 감소하지 않는다`() {
        val savedEnrollment = saveEnrollment("student")

        sut.cancelEnrollment(savedEnrollment.id, savedEnrollment.student.id)

        val foundEnrollment = enrollmentRepository.findByIdOrThrow(savedEnrollment.id)
        val foundClass = classRepository.findByIdOrThrow(savedEnrollment.enrolledClass.id)
        assertAll(
            { assertThat(foundEnrollment.enrollmentStatus).isEqualTo(EnrollmentStatus.CANCELLED) },
            { assertThat(foundClass.enrolledCount).isEqualTo(0) }
        )
    }

    @Test
    fun `정원이 가득 차면 수강 신청을 확정할 수 없다`() {
        val klass = classRepository.save(getClassFixture(1L).apply { open() })
        val firstEnrollment = enrollmentRepository.save(
            getEnrollmentFixture(
                klass,
                memberRepository.save(Member.create("student-1", MemberRole.STUDENT))
            )
        )
        val secondEnrollment = enrollmentRepository.save(
            getEnrollmentFixture(
                klass,
                memberRepository.save(Member.create("student-2", MemberRole.STUDENT))
            )
        )
        sut.completeEnrollment(firstEnrollment.id)

        val exception = catchThrowable { sut.completeEnrollment(secondEnrollment.id) }

        val foundSecondEnrollment = enrollmentRepository.findByIdOrThrow(secondEnrollment.id)
        val foundClass = classRepository.findByIdOrThrow(klass.id)
        assertAll(
            { assertThat(exception).isInstanceOf(com.liveklass.domain.ClassCapacityExceededException::class.java) },
            { assertThat(foundSecondEnrollment.enrollmentStatus).isEqualTo(EnrollmentStatus.PENDING) },
            { assertThat(foundClass.enrolledCount).isEqualTo(1) }
        )
    }

    @Test
    fun `본인 수강 신청이 아니면 취소할 수 없다`() {
        val savedEnrollment = saveEnrollment("student")
        val otherMember = memberRepository.save(Member.create("other-student", MemberRole.STUDENT))

        val exception = catchThrowable { sut.cancelEnrollment(savedEnrollment.id, otherMember.id) }

        val foundEnrollment = enrollmentRepository.findByIdOrThrow(savedEnrollment.id)
        assertAll(
            { assertThat(exception).isInstanceOf(EnrollmentAccessDeniedException::class.java) },
            { assertThat(foundEnrollment.enrollmentStatus).isEqualTo(EnrollmentStatus.PENDING) },
            { assertThat(foundEnrollment.cancelledDate).isNull() }
        )
    }

    @Test
    fun `회원 기준으로 내가 신청한 강의 목록을 조회할 수 있다`() {
        val firstEnrollment = saveEnrollment("student")
        val secondClass = classRepository.save(getClassFixture().apply { open() })
        enrollmentRepository.save(getEnrollmentFixture(secondClass, firstEnrollment.student))

        val response = sut.findEnrollmentsByMember(firstEnrollment.student.id)

        assertAll(
            { assertThat(response.responses).hasSize(2) },
            { assertThat(response.responses.map { it.enrollmentId }).contains(firstEnrollment.id) },
            { assertThat(response.responses.map { it.classId }).contains(firstEnrollment.enrolledClass.id, secondClass.id) },
            { assertThat(response.responses).allMatch { it.classTitle.isNotBlank() } }
        )
    }

    @Test
    fun `강의 기준으로 수강 신청한 학생 목록을 조회할 수 있다`() {
        val firstEnrollment = saveEnrollment("student-1")
        val secondStudent = memberRepository.save(Member.create("student-2", MemberRole.STUDENT))
        enrollmentRepository.save(getEnrollmentFixture(firstEnrollment.enrolledClass, secondStudent))

        val response = sut.findStudentsByClass(firstEnrollment.enrolledClass.id)

        assertAll(
            { assertThat(response.responses).hasSize(2) },
            { assertThat(response.responses.map { it.memberId }).contains(firstEnrollment.student.id, secondStudent.id) },
            { assertThat(response.responses.map { it.memberName }).contains("student-1", "student-2") },
            { assertThat(response.responses).allMatch { it.enrollmentStatus == EnrollmentStatus.PENDING } }
        )
    }

    private fun saveEnrollment(studentName: String) = enrollmentRepository.save(
        getEnrollmentFixture(
            classRepository.save(getClassFixture().apply { open() }),
            memberRepository.save(Member.create(studentName, MemberRole.STUDENT))
        )
    )
}
