package com.liveklass.application

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@DisplayName("대기열 SSE emitter 단위 테스트")
class WaitingQueueEmitterTest {

    @Test
    fun `emitter를 저장한 뒤 complete를 호출하면 제거된다`() {
        val sut = WaitingQueueSseEmitter()
        val emitter = SseEmitter()

        sut.save(1L, emitter)
        sut.complete(1L)

        assertThat(currentEmitterCount(sut)).isZero()
    }

    @Test
    fun `allowed 이벤트를 보내면 emitter가 제거된다`() {
        val sut = WaitingQueueSseEmitter()
        sut.save(1L, SseEmitter())

        sut.sendAllowed(1L)

        assertThat(currentEmitterCount(sut)).isZero()
    }

    @Test
    fun `expired 이벤트를 보내면 emitter가 제거된다`() {
        val sut = WaitingQueueSseEmitter()
        sut.save(1L, SseEmitter())

        sut.sendExpired(1L)

        assertThat(currentEmitterCount(sut)).isZero()
    }

    @Test
    fun `존재하지 않는 ticketId를 complete해도 예외가 발생하지 않는다`() {
        val sut = WaitingQueueSseEmitter()

        sut.complete(1L)

        assertThat(currentEmitterCount(sut)).isZero()
    }

    private fun currentEmitterCount(sut: WaitingQueueSseEmitter): Int {
        val field = WaitingQueueSseEmitter::class.java.getDeclaredField("emitters")
        field.isAccessible = true
        val emitters = field.get(sut) as Map<*, *>
        return emitters.size
    }
}
