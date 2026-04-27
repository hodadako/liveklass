package com.liveklass

import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

fun <T> runConcurrently(vararg tasks: () -> T): List<Result<T>> {
    val readyLatch = CountDownLatch(tasks.size)
    val startLatch = CountDownLatch(1)
    val doneLatch = CountDownLatch(tasks.size)
    val executor = Executors.newFixedThreadPool(tasks.size)
    val results = MutableList<Result<T>?>(tasks.size) { null }

    tasks.forEachIndexed { index, task ->
        executor.submit {
            readyLatch.countDown()
            startLatch.await()

            val result = runCatching { task() }
            synchronized(results) {
                results[index] = result
            }
            doneLatch.countDown()
        }
    }

    check(readyLatch.await(5, TimeUnit.SECONDS)) { "동시성 테스트 준비 시간이 초과되었습니다." }
    startLatch.countDown()
    check(doneLatch.await(10, TimeUnit.SECONDS)) { "동시성 테스트 실행 시간이 초과되었습니다." }
    executor.shutdownNow()

    return results.map { requireNotNull(it) }
}
