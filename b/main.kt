package b

import kotlinx.coroutines.*

fun main(): Unit = runBlocking {
    name()
    launch(context = CoroutineName("myCoroutine")) { name() }
}

fun name(){
    println("[${Thread.currentThread().name}]")
}