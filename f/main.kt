package f

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

@OptIn(ExperimentalStdlibApi::class)
fun main(): Unit = runBlocking {
    val coroutineContext: CoroutineContext = Dispatchers.IO + CoroutineName("MyCoroutine")

    val newCoroutineContext = coroutineContext.minusKey(CoroutineName)

    launch(coroutineContext) {
        println("name: ${coroutineContext[CoroutineName]}")
        println("dispatcher: ${coroutineContext[CoroutineDispatcher]}")
        println("job: ${coroutineContext[Job]}")
        println("exceptionHandler: ${coroutineContext[CoroutineExceptionHandler]}")
    }
    println(coroutineContext[CoroutineName] == coroutineContext[CoroutineName.Key])


}


// 1. ContextKey 역할
interface ContextKey<E>

// 2. ContextElement 역할
interface ContextElement {
    val key: ContextKey<*>
}

// 3. CustomContext 역할 (사전)
class CustomContext(private val elements: List<ContextElement>) {
    operator fun <E : ContextElement> get(key: ContextKey<E>): E? {
        return elements.firstOrNull { it.key == key } as? E
    }
}

// 4. CustomName (값 + 고유 키)
data class CustomName(val name: String) : ContextElement {
    companion object Key : ContextKey<CustomName> // 고유 키 정의

    override val key: ContextKey<*> get() = Key
}

fun a() {
    /*
    •	CustomName: 사람.
	    •	이 사람은 “이름표(Key)“를 가지고 다님.
	•	CustomContext: 사람들의 명단(리스트).
	•	coroutineContext[CustomName]: 특정 이름표(Key)를 가진 사람을 찾는 과정.
     */
    // CustomName 만들기 (이름표가 자동으로 생성됨)
    val customName = CustomName("Alice")

    // Context에 CustomName 추가
    val customContext = CustomContext(listOf(customName))

    // 이름표 없이 사람 찾기 (자동으로 이름표가 사용됨)
    val found = customContext[CustomName] // == customContext[CustomName.Key]
    println(found?.name) // 출력: Alice

}

