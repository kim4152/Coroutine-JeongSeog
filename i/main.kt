package i

import kotlinx.coroutines.*

fun main(): Unit = runBlocking {
    search()
}

suspend fun search() = supervisorScope {
    launch {
        searchFromDB()
    }
    launch {
        searchFromServer()
    }
}

