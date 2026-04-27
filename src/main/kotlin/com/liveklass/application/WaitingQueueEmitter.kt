package com.liveklass.application

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.ConcurrentHashMap

interface WaitingQueueEmitter {
    fun save(ticketId: Long, emitter: SseEmitter)
    fun sendAllowed(ticketId: Long)
    fun sendExpired(ticketId: Long)
    fun complete(ticketId: Long)
}

@Component
class WaitingQueueSseEmitter : WaitingQueueEmitter {
    private val log = LoggerFactory.getLogger(javaClass)
    private val emitters = ConcurrentHashMap<Long, SseEmitter>()

    override fun save(
        ticketId: Long,
        emitter: SseEmitter
    ) {
        emitters[ticketId] = emitter

        emitter.onCompletion {
            emitters.remove(ticketId)
        }

        emitter.onTimeout {
            emitters.remove(ticketId)
        }

        emitter.onError {
            emitters.remove(ticketId)
        }
    }

    override fun sendAllowed(ticketId: Long) {
        sendAndComplete(
            ticketId = ticketId,
            eventName = "allowed",
            data = mapOf("ticketId" to ticketId)
        )
    }

    override fun sendExpired(ticketId: Long) {
        sendAndComplete(
            ticketId = ticketId,
            eventName = "expired",
            data = mapOf("ticketId" to ticketId)
        )
    }

    override fun complete(ticketId: Long) {
        val emitter = emitters.remove(ticketId) ?: return

        try {
            emitter.complete()
        } catch (e: Exception) {
            log.warn("Failed to complete SSE emitter. ticketId={}", ticketId, e)
        }
    }

    private fun sendAndComplete(
        ticketId: Long,
        eventName: String,
        data: Any
    ) {
        val emitter = emitters.remove(ticketId) ?: return

        try {
            emitter.send(
                SseEmitter.event()
                    .name(eventName)
                    .data(data)
            )
        } catch (e: Exception) {
            log.warn("Failed to send SSE event. ticketId={}, eventName={}", ticketId, eventName, e)
        } finally {
            completeSafely(ticketId, emitter)
        }
    }

    private fun completeSafely(
        ticketId: Long,
        emitter: SseEmitter
    ) {
        try {
            emitter.complete()
        } catch (e: Exception) {
            log.warn("Failed to complete SSE emitter. ticketId={}", ticketId, e)
        }
    }
}
