package com.liveklass.dto

import java.time.LocalDateTime

data class CreateClassRequest(
    val title: String,
    val description: String,
    val price: Long,
    val capacity: Long,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime
)
