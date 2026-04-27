package com.liveklass.application

import com.liveklass.IntegrationTestSupport
import com.liveklass.domain.Enrollment
import com.liveklass.domain.EnrollmentStatus
import com.liveklass.domain.EnrollmentStatusException
import com.liveklass.domain.Member
import com.liveklass.domain.MemberRole
import com.liveklass.fixture.DomainTestFixture.getClassFixture
import com.liveklass.fixture.DomainTestFixture.getEnrollmentFixture
import com.liveklass.persistence.ClassRepository
import com.liveklass.persistence.EnrollmentRepository
import com.liveklass.persistence.MemberRepository
import com.liveklass.runConcurrently
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.ThrowableAssert.catchThrowable
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime

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
    fun `정원이 1명인 강의에서 동시에 두 건을 확정해도 한 명만 확정된다`() {
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

        val results = runConcurrently(
            { sut.completeEnrollment(firstEnrollment.id) },
            { sut.completeEnrollment(secondEnrollment.id) }
        )

        val foundClass = classRepository.findByIdOrThrow(klass.id)
        val foundEnrollments = listOf(
            enrollmentRepository.findByIdOrThrow(firstEnrollment.id),
            enrollmentRepository.findByIdOrThrow(secondEnrollment.id)
        )

        assertAll(
            { assertThat(results.count { it.isSuccess }).isEqualTo(1) },
            { assertThat(results.mapNotNull { it.exceptionOrNull() }).hasSize(1) },
            { assertThat(foundClass.enrolledCount).isEqualTo(1L) },
            { assertThat(foundEnrollments.count { it.enrollmentStatus == EnrollmentStatus.CONFIRMED }).isEqualTo(1) },
            { assertThat(foundEnrollments.count { it.enrollmentStatus == EnrollmentStatus.PENDING }).isEqualTo(1) }
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
    fun `같은 확정 수강 신청을 동시에 두 번 취소해도 좌석은 한 번만 반납된다`() {
        val savedEnrollment = saveEnrollment("student")
        sut.completeEnrollment(savedEnrollment.id)

        val results = runConcurrently(
            { sut.cancelEnrollment(savedEnrollment.id, savedEnrollment.student.id) },
            { sut.cancelEnrollment(savedEnrollment.id, savedEnrollment.student.id) }
        )

        val foundEnrollment = enrollmentRepository.findByIdOrThrow(savedEnrollment.id)
        val foundClass = classRepository.findByIdOrThrow(savedEnrollment.enrolledClass.id)

        assertAll(
            { assertThat(results.count { it.isSuccess }).isEqualTo(1) },
            { assertThat(results.mapNotNull { it.exceptionOrNull() }).hasSize(1) },
            { assertThat(results.mapNotNull { it.exceptionOrNull() }.first()).isInstanceOf(EnrollmentStatusException::class.java) },
            { assertThat(foundEnrollment.enrollmentStatus).isEqualTo(EnrollmentStatus.CANCELLED) },
            { assertThat(foundClass.enrolledCount).isEqualTo(0L) }
        )
    }

    @Test
    fun `회원 기준 조회는 최신순으로 20개까지 수강 신청 목록을 반환한다`() {
        val student = memberRepository.save(Member.create("student", MemberRole.STUDENT))
        val baseTime = LocalDateTime.of(2026, 4, 4, 12, 0)
        val enrollments = (1..22).map { index ->
            val klass = classRepository.save(getClassFixture().apply { open() })
            enrollmentRepository.save(Enrollment.create(klass, student, baseTime.plusDays(index.toLong())))
        }

        val response = sut.findEnrollmentsByMember(student.id)

        assertAll(
            { assertThat(response.responses).hasSize(20) },
            {
                assertThat(response.responses.map { it.enrollmentId })
                    .containsExactlyElementsOf(enrollments.takeLast(20).reversed().map { it.id })
            },
            {
                assertThat(response.responses.map { it.requestedDate })
                    .containsExactlyElementsOf(enrollments.takeLast(20).reversed().map { it.requestedDate })
            }
        )
    }

    @Test
    fun `강의 기준 확정 명단 조회는 최신순으로 20명까지 반환한다`() {
        val klass = classRepository.save(getClassFixture().apply { open() })
        val baseTime = LocalDateTime.of(2026, 4, 4, 12, 0)
        val confirmedEnrollments = (1..22).map { index ->
            val student = memberRepository.save(Member.create("student-$index", MemberRole.STUDENT))
            enrollmentRepository.save(
                Enrollment.create(klass, student, baseTime.plusDays(index.toLong())).apply {
                    confirm(requestedDate.plusHours(1))
                }
            )
        }
        enrollmentRepository.save(
            Enrollment.create(
                klass,
                memberRepository.save(Member.create("pending-student", MemberRole.STUDENT)),
                baseTime.plusDays(23)
            )
        )

        val response = sut.findStudentsByClass(klass.id)

        assertAll(
            { assertThat(response.responses).hasSize(20) },
            {
                assertThat(response.responses.map { it.enrollmentId })
                    .containsExactlyElementsOf(confirmedEnrollments.takeLast(20).reversed().map { it.id })
            },
            { assertThat(response.responses).allMatch { it.enrollmentStatus == EnrollmentStatus.CONFIRMED } }
        )
    }

    @Test
    fun `강의 기준 전체 수강 신청 조회는 최신순으로 20개까지 반환한다`() {
        val klass = classRepository.save(getClassFixture().apply { open() })
        val baseTime = LocalDateTime.of(2026, 4, 4, 12, 0)
        val enrollments = (1..22).map { index ->
            val student = memberRepository.save(Member.create("student-$index", MemberRole.STUDENT))
            enrollmentRepository.save(Enrollment.create(klass, student, baseTime.plusDays(index.toLong())))
        }
        enrollments[0].confirm(enrollments[0].requestedDate.plusHours(1))
        enrollments[1].cancel(enrollments[1].requestedDate.plusHours(1))

        val response = sut.findEnrollmentRequestsByClass(klass.id)

        assertAll(
            { assertThat(response.responses).hasSize(20) },
            {
                assertThat(response.responses.map { it.enrollmentId })
                    .containsExactlyElementsOf(enrollments.takeLast(20).reversed().map { it.id })
            },
            {
                assertThat(response.responses.map { it.requestedDate })
                    .containsExactlyElementsOf(enrollments.takeLast(20).reversed().map { it.requestedDate })
            }
        )
    }

    private fun saveEnrollment(studentName: String) = enrollmentRepository.save(
        getEnrollmentFixture(
            classRepository.save(getClassFixture().apply { open() }),
            memberRepository.save(Member.create(studentName, MemberRole.STUDENT))
        )
    )
}
