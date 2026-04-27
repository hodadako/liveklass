package com.liveklass.dto

import com.liveklass.domain.Enrollment
import com.liveklass.domain.EnrollmentStatus
import java.time.LocalDateTime

data class FindEnrollmentResponse(
    val enrollmentId: Long,
    val classId: Long,
    val classTitle: String,
    val enrollmentStatus: EnrollmentStatus,
    val requestedDate: LocalDateTime,
    val confirmedDate: LocalDateTime?,
    val cancelledDate: LocalDateTime?
) {
    companion object {
        fun from(enrollment: Enrollment): FindEnrollmentResponse = FindEnrollmentResponse(
            enrollmentId = enrollment.id,
            classId = enrollment.enrolledClass.id,
            classTitle = enrollment.enrolledClass.title,
            enrollmentStatus = enrollment.enrollmentStatus,
            requestedDate = enrollment.requestedDate,
            confirmedDate = enrollment.confirmedDate,
            cancelledDate = enrollment.cancelledDate
        )
    }
}
