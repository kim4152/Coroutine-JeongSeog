package k

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking

@Volatile
var count = 0

fun main() = runBlocking {
    val dispatcher = newSingleThreadContext("name")
    repeat(10000) {
        launch(dispatcher) {
            count++
        }
    }
    delay(1000)
    println(count)
}

