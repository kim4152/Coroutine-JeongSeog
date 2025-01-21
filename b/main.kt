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


fun main(): Unit = runBlocking {
    c()
}

fun name() {
    println("[${Thread.currentThread().name}]")
}