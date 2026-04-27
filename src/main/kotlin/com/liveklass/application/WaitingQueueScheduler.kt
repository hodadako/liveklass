package com.liveklass.application

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

interface WaitingQueueScheduler {
    fun processTicketsAndSend()
    fun deleteExpiredTickets()
}

@Component
class SimpleWaitingQueueScheduler(
    private val waitingQueueService: WaitingQueueService,
    private val waitingQueueEmitter: WaitingQueueEmitter
) : WaitingQueueScheduler {
    @Scheduled(fixedDelay = 1000)
    override fun processTicketsAndSend() {
        val ticketIds = waitingQueueService.findWaitingTickets()

        ticketIds.forEach {
            waitingQueueEmitter.sendAllowed(
                it
            )
        }
    }

    @Scheduled(fixedDelay = 1000)
    override fun deleteExpiredTickets() {
        val ticketIds = waitingQueueService.deleteExpiredTickets()

        ticketIds.forEach {
            waitingQueueEmitter.sendExpired(it)
        }
    }
}
