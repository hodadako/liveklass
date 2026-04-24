package com.liveklass.domain

sealed class DomainException(message: String) : RuntimeException(message)

class ClassStatusException(val classId: Long, val currentStatus: ClassStatus, val targetStatus: ClassStatus) : DomainException(
    "허용되지 않은 상태 변경입니다. classId=$classId, currentStatus=$currentStatus, targetStatus=$targetStatus"
)

class ClassEnrollmentException(val classId: Long, val currentStatus: ClassStatus) :
    DomainException(
        "강의가 모집 중 상태일때만 신청할 수 있습니다. classId=$classId, currentStatus=$currentStatus"
    )

class ClassCapacityExceededException(
    val classId: Long,
    val capacity: Int,
    val enrolledCount: Int
) : DomainException("강의 정원이 초과되었습니다. classId=$classId, capacity=$capacity, enrolledCount=$enrolledCount")
