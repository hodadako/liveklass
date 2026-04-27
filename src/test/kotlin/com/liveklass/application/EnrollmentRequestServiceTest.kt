package com.liveklass.application

import com.liveklass.IntegrationTestSupport
import com.liveklass.domain.EnrollmentStatus
import com.liveklass.domain.EnrollmentTicket
import com.liveklass.domain.EnrollmentTicketStatus
import com.liveklass.domain.EnrollmentTicketStatusException
import com.liveklass.domain.Member
import com.liveklass.domain.MemberRole
import com.liveklass.fixture.DomainTestFixture.getClassFixture
import com.liveklass.persistence.ClassRepository
import com.liveklass.persistence.EnrollmentRepository
import com.liveklass.persistence.EnrollmentTicketRepository
import com.liveklass.persistence.MemberRepository
import com.liveklass.runConcurrently
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime

@DisplayName("수강 신청 요청 서비스 통합 테스트")
class EnrollmentRequestServiceTest @Autowired constructor(
    private val sut: EnrollmentRequestUseCase,
    private val classRepository: ClassRepository,
    private val memberRepository: MemberRepository,
    private val enrollmentRepository: EnrollmentRepository,
    private val enrollmentTicketRepository: EnrollmentTicketRepository
) : IntegrationTestSupport() {

    @Test
    fun `수강 신청 요청은 대기 상태 수강 신청을 만들고 좌석 수는 증가하지 않는다`() {
        val klass = classRepository.save(getClassFixture().apply { open() })
        val student = memberRepository.save(Member.create("student", MemberRole.STUDENT))
        val ticket = enrollmentTicketRepository.save(
            EnrollmentTicket.create(klass.id, student.id).apply {
                allow(LocalDateTime.now(), 5)
            }
        )

        val response = sut.requestEnrollment(klass.id, student.id, ticket.id)

        val foundEnrollment = enrollmentRepository.findByIdOrThrow(response.enrollmentId)
        val foundClass = classRepository.findByIdOrThrow(klass.id)
        assertAll(
            { assertThat(foundEnrollment.enrollmentStatus).isEqualTo(EnrollmentStatus.PENDING) },
            { assertThat(foundEnrollment.student.id).isEqualTo(student.id) },
            { assertThat(foundEnrollment.enrolledClass.id).isEqualTo(klass.id) },
            { assertThat(foundClass.enrolledCount).isEqualTo(0) }
        )
    }

    @Test
    fun `같은 대기열 티켓으로 동시에 수강 신청하면 하나만 성공한다`() {
        val klass = classRepository.save(getClassFixture().apply { open() })
        val student = memberRepository.save(Member.create("student", MemberRole.STUDENT))
        val ticket = enrollmentTicketRepository.save(
            EnrollmentTicket.create(klass.id, student.id).apply {
                allow(LocalDateTime.now(), 5)
            }
        )

        val results = runConcurrently(
            { sut.requestEnrollment(klass.id, student.id, ticket.id) },
            { sut.requestEnrollment(klass.id, student.id, ticket.id) }
        )

        val successCount = results.count { it.isSuccess }
        val failures = results.mapNotNull { it.exceptionOrNull() }
        val foundTicket = enrollmentTicketRepository.findByIdOrThrow(ticket.id)

        assertAll(
            { assertThat(successCount).isEqualTo(1) },
            { assertThat(failures).hasSize(1) },
            { assertThat(failures[0]).isInstanceOf(EnrollmentTicketStatusException::class.java) },
            { assertThat(enrollmentRepository.count()).isEqualTo(1L) },
            { assertThat(foundTicket.status).isEqualTo(EnrollmentTicketStatus.USED) }
        )
    }
}
