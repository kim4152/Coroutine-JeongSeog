package h

import kotlinx.coroutines.*

fun a() = runBlocking {
    val supervisorJob = SupervisorJob(this.coroutineContext[Job])
    launch(CoroutineName("Coroutine1") + supervisorJob) {
        launch(CoroutineName("Coroutine3")) {
            //throw Exception("")
        }
        delay(100)
        println("1")
    }

    launch(CoroutineName("Coroutine2") + supervisorJob) {
        delay(10)
        println("2")
    }
    supervisorJob.complete()
}

fun b() = runBlocking<Unit> {
    val coroutineScope = CoroutineScope(SupervisorJob())
    coroutineScope.apply {
        launch(CoroutineName("Coroutine1")) {
            launch(CoroutineName("Coroutine2")) {
                throw Exception("exception")
            }
            delay(1)
            println("111")
        }

        launch(CoroutineName("Coroutine3")) {
            delay(1)
            println("333")
        }
    }
    delay(1000)
}

fun c() = runBlocking {
    val supervisorJob = SupervisorJob(this.coroutineContext[Job])
    launch(CoroutineName("Coroutine1") + supervisorJob) {
        launch(CoroutineName("Coroutine3")) {
            throw Exception("")
        }
        delay(100)
        println("1")
    }

    launch(CoroutineName("Coroutine2") + supervisorJob) {
        println("2")
    }
    delay(1_000)
}

fun d() = runBlocking {
    launch(CoroutineName("ParentCoroutine") + SupervisorJob()) {
        launch(CoroutineName("Coroutine1")) {
            launch { throw Exception("error") }
            delay(10)
            println("111")
        }
        launch(CoroutineName("Coroutine2")) {
            delay(10)
            println("222")
        }
    }
    delay(1_000)
}

fun e() = runBlocking {
    val context = Job() + CoroutineExceptionHandler { context, throwable ->
        println("exception : ${throwable}")
    }
    launch(CoroutineName("coroutine1") + context) {
        throw Exception("exception")
    }
    delay(1_000)
}

fun f() = runBlocking {
    val handler = CoroutineExceptionHandler { context, throwable ->
        println("exception : ${throwable}")
    }
    val supervisedScope = CoroutineScope(SupervisorJob() + handler)
    val result = supervisedScope.async {
        throw Exception("error")
        "async"
    }
    result.cancel()
    println(result.await())
//    try {
//        println(result.await())
//    } catch (e: Exception) {
//        println(e)
//    }
}

fun main() {
    f()
}
