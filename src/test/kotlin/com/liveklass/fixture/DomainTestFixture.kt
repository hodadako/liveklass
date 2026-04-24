package com.liveklass.fixture

import com.liveklass.domain.Class
import com.liveklass.domain.Member
import com.liveklass.domain.MemberRole
import java.time.LocalDateTime

object DomainTestFixture {
    fun getTestClass(): Class = Class.create(
        "쉽게 시작하는 Kotlin",
        "처음 배우는 Kotlin",
        100000,
        1,
        LocalDateTime.now(),
        LocalDateTime.now().plusDays(1)
    )

    fun getTestMember(): Member = Member.create(
        "호다코",
        MemberRole.CREATOR
    )
}
