package i

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope

fun main(): Unit = runBlocking {
    search()
}

suspend fun search() = supervisorScope {

}

