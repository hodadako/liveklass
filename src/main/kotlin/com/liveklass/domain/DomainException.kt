package com.liveklass.domain

const val STATUS_EXCEPTION_PREFIX = "허용되지 않은 상태 변경입니다."

sealed class DomainException(message: String) : RuntimeException(message)

class ClassStatusException(val classId: Long, val currentStatus: ClassStatus, val targetStatus: ClassStatus) :
    DomainException(
        STATUS_EXCEPTION_PREFIX + "classId=$classId, currentStatus=$currentStatus, targetStatus=$targetStatus"
    )

class ClassEnrollmentException(val classId: Long, val currentStatus: ClassStatus) :
    DomainException(
        "강의가 모집 중 상태일때만 신청할 수 있습니다. classId=$classId, currentStatus=$currentStatus"
    )

class ClassCapacityExceededException(
    val classId: Long,
    val capacity: Long,
    val enrolledCount: Long
) : DomainException("강의 정원이 초과되었습니다. classId=$classId, capacity=$capacity, enrolledCount=$enrolledCount")

class EnrollmentStatusException(
    val enrollmentId: Long,
    val currentStatus: EnrollmentStatus,
    val targetStatus: EnrollmentStatus
) :
    DomainException(
        STATUS_EXCEPTION_PREFIX + "enrollmentId=$enrollmentId, currentStatus=$currentStatus, targetStatus=$targetStatus"
    )

class EnrollmentCancelException(val enrollmentId: Long) :
    DomainException("수강 신청 취소 가능 기간이 지났습니다. enrollmentId=$enrollmentId")

class EnrollmentTicketStatusException(
    val enrollmentTicketId: Long,
    val currentStatus: EnrollmentTicketStatus,
    val targetStatus: EnrollmentTicketStatus
) : DomainException(STATUS_EXCEPTION_PREFIX + "enrollmentTicketId=$enrollmentTicketId, currentStatus=$currentStatus, targetStatus=$targetStatus")

class EnrollmentTicketExpiredException(
    val enrollmentTicketId: Long
) : DomainException("대기열이 만료되었습니다. enrollmentTicketId=$enrollmentTicketId")
