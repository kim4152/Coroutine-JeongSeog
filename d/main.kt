package b

import kotlinx.coroutines.*
import kotlin.coroutines.EmptyCoroutineContext

fun main(): Unit = runBlocking {
    val job = launch(
        start = CoroutineStart.LAZY
    ) {  }
}

fun ff(vararg ints: Int){

}