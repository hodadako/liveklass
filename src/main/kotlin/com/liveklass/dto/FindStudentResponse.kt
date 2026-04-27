package com.liveklass.dto

import com.liveklass.domain.Enrollment
import com.liveklass.domain.EnrollmentStatus
import java.time.LocalDateTime

data class FindStudentResponse(
    val enrollmentId: Long,
    val memberId: Long,
    val memberName: String,
    val enrollmentStatus: EnrollmentStatus,
    val requestedDate: LocalDateTime,
    val confirmedDate: LocalDateTime?,
    val cancelledDate: LocalDateTime?
) {
    companion object {
        fun from(enrollment: Enrollment): FindStudentResponse = FindStudentResponse(
            enrollmentId = enrollment.id,
            memberId = enrollment.student.id,
            memberName = enrollment.student.name,
            enrollmentStatus = enrollment.enrollmentStatus,
            requestedDate = enrollment.requestedDate,
            confirmedDate = enrollment.confirmedDate,
            cancelledDate = enrollment.cancelledDate
        )
    }
}
