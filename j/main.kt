package j

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val job = launch {
        println("1")
        delay(1_000)
        println("2")
    }
    println("3")
    job.join()
    println("4")
}