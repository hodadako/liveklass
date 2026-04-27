package com.liveklass.dto

import com.liveklass.domain.Enrollment

data class FindStudentListResponse(
    val responses: List<FindStudentResponse>
) {
    companion object {
        fun from(enrollments: List<Enrollment>): FindStudentListResponse =
            FindStudentListResponse(enrollments.map(FindStudentResponse::from))
    }
}
