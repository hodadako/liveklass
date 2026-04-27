package com.liveklass.dto

import com.liveklass.domain.Enrollment

data class FindEnrollmentListResponse(
    val responses: List<FindEnrollmentResponse>
) {
    companion object {
        fun from(enrollments: List<Enrollment>): FindEnrollmentListResponse =
            FindEnrollmentListResponse(enrollments.map(FindEnrollmentResponse::from))
    }
}
