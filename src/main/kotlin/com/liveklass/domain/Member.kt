package com.liveklass.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated

@Entity
class Member private constructor(
    @Column(nullable = false)
    val name: String,
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val memberRole: MemberRole
) : BaseEntity() {
    companion object {
        fun create(name: String, memberRole: MemberRole): Member = Member(name, memberRole)
    }
}
