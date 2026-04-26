package com.liveklass.dto

import com.liveklass.domain.Class
import com.liveklass.domain.ClassStatus
import java.time.LocalDateTime

data class FindClassResponse(
    val classId: Long,
    val title: String,
    val description: String,
    val classStatus: ClassStatus,
    val price: Long,
    val enrolledCount: Long,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime
) {
    companion object {
        fun from(foundClass: Class): FindClassResponse = FindClassResponse(
            classId = foundClass.id,
            title = foundClass.title,
            description = foundClass.description,
            classStatus = foundClass.classStatus,
            price = foundClass.price,
            enrolledCount = foundClass.enrolledCount,
            startDate = foundClass.startDate,
            endDate = foundClass.endDate
        )
    }
}
