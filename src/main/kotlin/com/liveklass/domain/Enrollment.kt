package com.liveklass.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import java.time.LocalDateTime

@Entity
class Enrollment private constructor(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id")
    val enrolledClass: Class,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    val student: Member,
    @Column
    @Enumerated(EnumType.STRING)
    var enrollmentStatus: EnrollmentStatus = EnrollmentStatus.PENDING,
    @Column
    var requestedDate: LocalDateTime,
    @Column
    var confirmedDate: LocalDateTime? = null,
    @Column
    var cancelledDate: LocalDateTime? = null
) : BaseEntity() {
    companion object {
        fun create(enrolledClass: Class, student: Member, requestedDate: LocalDateTime): Enrollment = Enrollment(
            enrolledClass = enrolledClass,
            student = student,
            requestedDate = requestedDate
        )
    }

    fun confirm(now: LocalDateTime) {
        if (enrollmentStatus != EnrollmentStatus.PENDING) {
            throw EnrollmentStatusException(id, enrollmentStatus, EnrollmentStatus.CONFIRMED)
        }
        enrollmentStatus = EnrollmentStatus.CONFIRMED
        confirmedDate = now
    }

    fun cancel(now: LocalDateTime) {
        if (enrollmentStatus != EnrollmentStatus.PENDING && enrollmentStatus != EnrollmentStatus.CONFIRMED) {
            throw EnrollmentStatusException(id, enrollmentStatus, EnrollmentStatus.CANCELLED)
        }

        if (requestedDate.plusDays(7).isBefore(now)) {
            throw EnrollmentCancelException(id)
        }
        enrollmentStatus = EnrollmentStatus.CANCELLED
        cancelledDate = now
    }
}
