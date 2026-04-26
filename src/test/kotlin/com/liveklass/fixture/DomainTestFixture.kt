package com.liveklass.fixture

import com.liveklass.domain.Class
import com.liveklass.domain.Enrollment
import com.liveklass.domain.Member
import com.liveklass.domain.MemberRole
import java.time.LocalDateTime

object DomainTestFixture {
    private val BASE_TIME = LocalDateTime.of(2026, 12, 10, 6, 28)

    fun getClassListFixture(): List<Class> = listOf(
        Class.create(
            "쉽게 시작하는 Kotlin",
            "처음 배우는 Kotlin",
            100000,
            30,
            BASE_TIME,
            BASE_TIME.plusDays(1)
        ),
        Class.create(
            "쉽게 시작하는 TypeScript",
            "처음 배우는 TypeScript",
            100000,
            30,
            BASE_TIME,
            BASE_TIME.plusDays(1)
        ),
        Class.create(
            "쉽게 시작하는 Java",
            "처음 배우는 Java",
            100000,
            30,
            BASE_TIME,
            BASE_TIME.plusDays(1)
        ),
        Class.create(
            "쉽게 시작하는 JavaScript",
            "처음 배우는 JavaScript",
            100000,
            30,
            BASE_TIME,
            BASE_TIME.plusDays(1)
        ),
        Class.create(
            "쉽게 시작하는 C#",
            "처음 배우는 C#",
            100000,
            30,
            BASE_TIME,
            BASE_TIME.plusDays(1)
        ),
        Class.create(
            "쉽게 시작하는 Go",
            "처음 배우는 Go",
            100000,
            30,
            BASE_TIME,
            BASE_TIME.plusDays(1)
        )
    )
    fun getEnrollmentListFixture(
        classes: List<Class>,
        members: List<Member>
    ): List<Enrollment> = listOf(
        Enrollment.create(classes[0], members[0], BASE_TIME),
        Enrollment.create(classes[0], members[1], BASE_TIME.plusMinutes(1)),
        Enrollment.create(classes[0], members[2], BASE_TIME.plusMinutes(2)),
        Enrollment.create(classes[0], members[3], BASE_TIME.plusMinutes(3)),
        Enrollment.create(classes[0], members[4], BASE_TIME.plusMinutes(4)),
        Enrollment.create(classes[0], members[5], BASE_TIME.plusMinutes(5)),

        Enrollment.create(classes[1], members[0], BASE_TIME),
        Enrollment.create(classes[1], members[3], BASE_TIME.plusMinutes(1)),

        Enrollment.create(classes[2], members[0], BASE_TIME),
        Enrollment.create(classes[2], members[4], BASE_TIME),
        Enrollment.create(classes[2], members[5], BASE_TIME.plusMinutes(1)),

        Enrollment.create(classes[3], members[0], BASE_TIME),
        Enrollment.create(classes[3], members[1], BASE_TIME),

        Enrollment.create(classes[4], members[0], BASE_TIME),
        Enrollment.create(classes[4], members[2], BASE_TIME),

        Enrollment.create(classes[5], members[0], BASE_TIME)
    )
    fun getMemberListFixture(): List<Member> = listOf(
        Member.create(
            "stu1",
            MemberRole.STUDENT
        ),
        Member.create(
            "stu2",
            MemberRole.STUDENT
        ),
        Member.create(
            "stu3",
            MemberRole.STUDENT
        ),
        Member.create(
            "stu4",
            MemberRole.STUDENT
        ),
        Member.create(
            "stu5",
            MemberRole.STUDENT
        ),
        Member.create(
            "stu6",
            MemberRole.STUDENT
        )
    )

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

    fun getEnrollmentFixture(enrolledClass: Class, student: Member): Enrollment = Enrollment.create(
        enrolledClass,
        student,
        BASE_TIME
    )
}
