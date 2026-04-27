package com.liveklass.dto

data class EnterWaitingQueueResponse(
    val ticketId: Long,
    val position: Long,
    val waitingCount: Long,
    val estimatedWaitingTime: Long
)
