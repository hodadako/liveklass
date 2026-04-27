package com.liveklass.application

import com.liveklass.domain.Enrollment
import com.liveklass.dto.RequestEnrollmentResponse
import com.liveklass.persistence.ClassRepository
import com.liveklass.persistence.EnrollmentRepository
import com.liveklass.persistence.EnrollmentTicketRepository
import com.liveklass.persistence.MemberRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

interface EnrollmentRequestUseCase {
    fun requestEnrollment(
        classId: Long,
        memberId: Long,
        ticketId: Long
    ): RequestEnrollmentResponse
}

@Service
class EnrollmentRequestService(
    private val classRepository: ClassRepository,
    private val memberRepository: MemberRepository,
    private val enrollmentRepository: EnrollmentRepository,
    private val enrollmentTicketRepository: EnrollmentTicketRepository
) : EnrollmentRequestUseCase {
    @Transactional
    override fun requestEnrollment(classId: Long, memberId: Long, ticketId: Long): RequestEnrollmentResponse {
        val now = LocalDateTime.now()
        val foundClass = classRepository.findByIdOrThrow(classId)
        val foundMember = memberRepository.findByIdOrThrow(memberId)
        val foundTicket = enrollmentTicketRepository.findByIdOrThrow(ticketId)

        if (foundTicket.classId != foundClass.id || foundTicket.memberId != memberId) {
            throw InvalidTicketException(classId, memberId, ticketId)
        }

        foundClass.validateEnrollmentRequest()
        foundTicket.use(now)

        val savedEnrollment = enrollmentRepository.save(
            Enrollment.create(
                foundClass,
                foundMember,
                now
            )
        )

        return RequestEnrollmentResponse(
            enrollmentId = savedEnrollment.id
        )
    }
}
