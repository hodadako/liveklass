package com.liveklass.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import java.time.LocalDateTime

@Entity
class EnrollmentTicket private constructor(
    @Column(nullable = false)
    val classId: Long,
    @Column(nullable = false)
    val memberId: Long,
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var status: EnrollmentTicketStatus = EnrollmentTicketStatus.WAITING,
    @Column
    var allowedAt: LocalDateTime? = null,
    @Column
    var expiresAt: LocalDateTime? = null,
    @Column
    var usedAt: LocalDateTime? = null
) : BaseEntity() {
    companion object {
        fun create(
            classId: Long,
            memberId: Long
        ): EnrollmentTicket = EnrollmentTicket(classId, memberId)
    }

    fun use(now: LocalDateTime) {
        if (status != EnrollmentTicketStatus.ALLOWED) {
            throw EnrollmentTicketStatusException(id, status, EnrollmentTicketStatus.USED)
        }
        if (expiresAt == null || now.isAfter(expiresAt)) {
            status = EnrollmentTicketStatus.EXPIRED
            throw EnrollmentTicketExpiredException(id)
        }

        status = EnrollmentTicketStatus.USED
        usedAt = now
    }

    fun allow(now: LocalDateTime, ttl: Long) {
        if (status != EnrollmentTicketStatus.WAITING) {
            throw EnrollmentTicketStatusException(id, status, EnrollmentTicketStatus.ALLOWED)
        }
        status = EnrollmentTicketStatus.ALLOWED
        allowedAt = now
        expiresAt = now.plusMinutes(ttl)
    }
}
