package a

import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

fun main() {
    runBlocking {
        println("시작::활성화 된 스레드 갯수 = ${Thread.activeCount()}")
        val time = measureTimeMillis {
            val jobs = ArrayList<Job>()
            repeat(1000) {
                jobs += launch(Dispatchers.Default) {
                    delay(1000L)
                }
            }
            println("끝::활성화 된 스레드 갯수 = ${Thread.activeCount()}")
            jobs.forEach { it.join() }
        }
        println("Took $time ms")
    }
    runBlocking {
        println("시작::활성화 된 스레드 갯수 = ${Thread.activeCount()}")
        val time = measureTimeMillis {
            val jobs = ArrayList<Thread>()
            repeat(1000) {
                jobs += Thread {
                    Thread.sleep(1000L)
                }.also { it.start() }
            }
            println("끝::활성화 된 스레드 갯수 = ${Thread.activeCount()}")
            jobs.forEach { it.join() }
        }
        println("Took $time ms")
    }
}