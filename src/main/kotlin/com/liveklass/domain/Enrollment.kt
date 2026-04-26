package com.liveklass.domain

import jakarta.persistence.Column
import jakarta.persistence.ConstraintMode
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDateTime

@Entity
@Table(
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_enrollment_class_member",
            columnNames = ["class_id", "member_id"]
        )
    ]
)
class Enrollment private constructor(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "class_id",
        nullable = false,
        updatable = false,
        foreignKey = jakarta.persistence.ForeignKey(
            ConstraintMode.NO_CONSTRAINT
        )
    )
    val enrolledClass: Class,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "member_id",
        nullable = false,
        updatable = false,
        foreignKey = jakarta.persistence.ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    val student: Member,
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var enrollmentStatus: EnrollmentStatus = EnrollmentStatus.PENDING,
    @Column(nullable = false, updatable = false)
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
