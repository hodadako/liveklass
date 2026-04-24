package com.liveklass.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import java.time.LocalDateTime

@Entity
class Class private constructor(
    @Column
    var title: String,
    @Column
    var description: String,
    @Column
    @Enumerated(EnumType.STRING)
    var classStatus: ClassStatus = ClassStatus.DRAFT,
    @Column
    var price: Int,
    @Column
    var capacity: Int,
    @Column
    val startDate: LocalDateTime,
    @Column
    val endDate: LocalDateTime
) : BaseEntity() {
    companion object {
        fun create(
            title: String,
            description: String,
            price: Int,
            capacity: Int,
            startDate: LocalDateTime,
            endDate: LocalDateTime
        ): Class {
            require(title.isNotBlank()) { "강의 제목은 0자 이상 입력해야합니다." }
            require(price >= 0) { "가격은 0원 이상이어야 합니다." }
            require(capacity > 0) { "수강 정원은 0명 이상이어야 합니다." }
            require(startDate.isBefore(endDate)) { "수강 시작일은 종료일보다 빨라야 합니다." }

            return Class(
                title = title,
                description = description,
                classStatus = ClassStatus.DRAFT,
                price = price,
                capacity = capacity,
                startDate = startDate,
                endDate = endDate
            )
        }
    }

    fun open() {
        if (classStatus != ClassStatus.DRAFT) {
            throw ClassStatusException(id, classStatus, ClassStatus.OPEN)
        }
        classStatus = ClassStatus.OPEN
    }

    fun close() {
        if (classStatus != ClassStatus.OPEN) {
            throw ClassStatusException(id, classStatus, ClassStatus.CLOSED)
        }
        classStatus = ClassStatus.CLOSED
    }
}
