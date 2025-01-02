package b

import kotlinx.coroutines.*
import kotlin.coroutines.EmptyCoroutineContext

fun main(): Unit = runBlocking {
    name()
    launch(context = CoroutineName("myCoroutine")) { name() }
    val a = EmptyCoroutineContext
}

fun name(){
    println("[${Thread.currentThread().name}]")
}