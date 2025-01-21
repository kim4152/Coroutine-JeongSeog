package b

import kotlinx.coroutines.*


fun c() = runBlocking {
    val handler = CoroutineExceptionHandler { _, _ ->

    }
    val scope = CoroutineScope(Dispatchers.IO + Job() + handler)
    val job = scope.async {
        1
    }
    try {
        job.await() / 0
    } catch (e: Exception) {
        println("11")
        return@runBlocking
    }
    println("${job.await()}")
}

fun d() = runBlocking {
    name()
    withContext(Dispatchers.Default) {
        name()
    }
}

fun e() = runBlocking() {
    name()
    val job = launch(
        context = Dispatchers.IO,
        start = CoroutineStart.UNDISPATCHED
    ) {
        name()
        delay(10)
        name()
    }
}

fun main(): Unit = runBlocking {
    e()
}

fun name() {
    println("[${Thread.currentThread().name}]")
}