package com.liveklass.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated

@Entity
class Member private constructor(
    @Column
    val name: String,
    @Column
    @Enumerated(EnumType.STRING)
    val memberRole: MemberRole
) : BaseEntity()
