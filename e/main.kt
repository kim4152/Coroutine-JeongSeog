package e

import b.name
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

fun main() = runBlocking<Unit> {
    name()
    val time = measureTimeMillis {
        withContext(Dispatchers.IO){
            name()
            delay(1_000)
        }
        withContext(Dispatchers.IO){
            name()
            delay(1_000)
        }
    }
    async {
        name()
        println("async")
    }
    println("took $time ms")
}