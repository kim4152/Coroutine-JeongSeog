package g

import b.name
import kotlinx.coroutines.*

fun a() = runBlocking {
    val parentJob = launch(Dispatchers.IO) {
        val job1 = async { }
        val job2 = async { }
        val dbResultsDeffered: List<Deferred<String>> = listOf(job1, job2).map { it ->
            async {
                delay(1_000)
                return@async "$it"
            }
        }
        val result = dbResultsDeffered.awaitAll()
        println(result.joinToString())
    }
    parentJob.cancel()
}

fun b() = runBlocking {
    val coroutineContext = newSingleThreadContext("My Thread") + CoroutineName("CoroutineA")
    launch(coroutineContext) {
        name()
        launch {
            name()
        }
    }
}

fun c() = runBlocking {
    val coroutineContext = newSingleThreadContext("My Thread") + CoroutineName("ParentCoroutine")
    launch(coroutineContext) {
        name()
        launch(CoroutineName("ChildCoroutine")) {
            name()
        }
    }
}

fun d() = runBlocking {
    val runBlockingJob = coroutineContext[Job]
    launch {
        val launchJob = coroutineContext[Job]
        println(runBlockingJob == launchJob)
    }
}

fun e() = runBlocking {
    val parentJob = coroutineContext[Job]
    launch {
        val childJob = coroutineContext[Job]
        println(parentJob?.children?.contains(childJob))
        println(childJob?.parent == parentJob)
    }
}

fun f() = runBlocking {
    val a = launch { }
    val b = launch { }
    println(a.parent == b.parent)
}

fun main() {
    f()
}
