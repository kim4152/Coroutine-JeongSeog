package c

import b.name
import kotlinx.coroutines.*

fun a(): Unit = runBlocking {
    val singleThreadDispatcher: CoroutineDispatcher = newSingleThreadContext(name = "Parent")
    launch(context = singleThreadDispatcher) {
        name()
    }

    val multiThreadDispatcher: CoroutineDispatcher = newFixedThreadPoolContext(
        nThreads = 2,
        name = "MultiThread"
    )
    repeat(3){
        launch(context = multiThreadDispatcher) { name() }
    }
}

fun b(): Unit = runBlocking{
    val singleThreadDispatcher: CoroutineDispatcher = newSingleThreadContext(name = "Parent")
    launch(context = singleThreadDispatcher) {
        name() // 부모 코루틴
        launch { name() } // 자식 코루틴 실행
        launch { name() } // 자식 코루틴 실행
    }
}

fun main(): Unit = runBlocking {
    repeat(2){
        launch(Dispatchers.Default) { name() }
        launch(Dispatchers.IO) { name() }
    }
}
