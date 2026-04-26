package com.liveklass.fixture

import com.liveklass.dto.CreateClassRequest
import java.time.LocalDateTime

object ServiceTestFixture {
    private val BASE_TIME = LocalDateTime.of(2026, 1, 5, 10, 20, 10)
    fun getCreateClassRequestFixture(): CreateClassRequest = CreateClassRequest(
        title = "쉽게 시작하는 Kotlin",
        description = "처음 배우는 Kotlin",
        price = 100_000,
        capacity = 1,
        startDate = BASE_TIME,
        endDate = BASE_TIME.plusDays(5)
    )
}
