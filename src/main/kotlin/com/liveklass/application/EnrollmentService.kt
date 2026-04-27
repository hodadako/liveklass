package com.liveklass.application

import com.liveklass.domain.EnrollmentStatus
import com.liveklass.dto.FindEnrollmentListResponse
import com.liveklass.dto.FindStudentListResponse
import com.liveklass.persistence.ClassRepository
import com.liveklass.persistence.EnrollmentRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

private const val ENROLLMENT_READ_PAGE_SIZE = 20
private val LATEST_ENROLLMENT_PAGE: Pageable = PageRequest.of(
    0,
    ENROLLMENT_READ_PAGE_SIZE,
    Sort.by(
        Sort.Order.desc("requestedDate"),
        Sort.Order.desc("id")
    )
)

interface EnrollmentUseCase {
    fun completeEnrollment(enrollmentId: Long)
    fun cancelEnrollment(enrollmentId: Long, memberId: Long)
    fun findEnrollmentsByMember(memberId: Long): FindEnrollmentListResponse
    fun findEnrollmentRequestsByClass(classId: Long): FindEnrollmentListResponse
    fun findStudentsByClass(classId: Long): FindStudentListResponse
}

@Service
class EnrollmentService(
    private val classRepository: ClassRepository,
    private val enrollmentRepository: EnrollmentRepository
) : EnrollmentUseCase {
    @Transactional
    override fun completeEnrollment(enrollmentId: Long) {
        val foundEnrollment = enrollmentRepository.findByIdForUpdate(enrollmentId)
            ?: throw EntityNotFoundException(enrollmentId, "Enrollment")
        val foundClass = classRepository.findByIdForUpdate(foundEnrollment.enrolledClass.id)
            ?: throw EntityNotFoundException(foundEnrollment.enrolledClass.id, "Class(강의)")

        foundClass.confirmEnrollment()
        foundEnrollment.confirm(LocalDateTime.now())
    }

    @Transactional
    override fun cancelEnrollment(enrollmentId: Long, memberId: Long) {
        val foundEnrollment = enrollmentRepository.findByIdForUpdate(enrollmentId)
            ?: throw EntityNotFoundException(enrollmentId, "Enrollment")

        if (foundEnrollment.student.id != memberId) {
            throw EnrollmentAccessDeniedException(enrollmentId, memberId)
        }

        val shouldReleaseSeat = foundEnrollment.enrollmentStatus == EnrollmentStatus.CONFIRMED
        val foundClass = if (shouldReleaseSeat) {
            classRepository.findByIdForUpdate(foundEnrollment.enrolledClass.id)
                ?: throw EntityNotFoundException(foundEnrollment.enrolledClass.id, "Class(강의)")
        } else {
            null
        }

        foundEnrollment.cancel(LocalDateTime.now())

        if (shouldReleaseSeat) {
            foundClass!!.cancelEnrollment()
        }
    }

    @Transactional(readOnly = true)
    override fun findEnrollmentsByMember(memberId: Long): FindEnrollmentListResponse =
        FindEnrollmentListResponse.from(enrollmentRepository.findAllByMember(memberId, LATEST_ENROLLMENT_PAGE).content)

    @Transactional(readOnly = true)
    override fun findEnrollmentRequestsByClass(classId: Long): FindEnrollmentListResponse =
        FindEnrollmentListResponse.from(enrollmentRepository.findAllByClass(classId, LATEST_ENROLLMENT_PAGE).content)

    @Transactional(readOnly = true)
    override fun findStudentsByClass(classId: Long): FindStudentListResponse =
        FindStudentListResponse.from(
            enrollmentRepository.findAllByClassAndStatus(classId, EnrollmentStatus.CONFIRMED, LATEST_ENROLLMENT_PAGE).content
        )
}
