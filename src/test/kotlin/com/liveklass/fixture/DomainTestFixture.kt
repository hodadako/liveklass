package com.liveklass.fixture

import com.liveklass.domain.Class
import com.liveklass.domain.Member
import com.liveklass.domain.MemberRole
import java.time.LocalDateTime

object DomainTestFixture {
    private val BASE_TIME = LocalDateTime.of(2026, 12, 10, 6, 28)

    fun getClassFixture(capacity: Long = 30L): Class = Class.create(
        "쉽게 시작하는 Kotlin",
        "처음 배우는 Kotlin",
        100000,
        capacity,
        BASE_TIME,
        BASE_TIME.plusDays(1)
    )

    fun getMemberFixture(): Member = Member.create(
        "호다코",
        MemberRole.CREATOR
    )
}
