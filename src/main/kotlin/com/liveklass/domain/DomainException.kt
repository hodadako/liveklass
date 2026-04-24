package com.liveklass.domain

sealed class DomainException(message: String) : RuntimeException(message)

class ClassStatusException(val classId: Long, val currentStatus: ClassStatus, val targetStatus: ClassStatus) : DomainException(
    "허용되지 않은 상태 변경입니다. classId=$classId, currentStatus=$currentStatus, targetStatus=$targetStatus"
)
