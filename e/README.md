# async와 Deferred

#### async

launch와 다른 점은 async은 결과값이 있는 코루틴 객체인 Deferred가 반환된다.  
이를 통해 코루틴으로부터 결과값을 수신할 수 있다.

#### Deferred

Deferred는 Job 인터페이스를 구현하기 때문에 Job 객체의 모든 함수와 프로퍼티를 사용할 수 있다.

```kotlin
public interface Deferred<out T> : Job
```

#### await

Deferred 객체는 결괏값 수신의 대기를 위해 await 함수를 제공한다.

대상 코루틴이 실행 완료될 때까지 await 함수를 호출한 코루틴을 일시 중단하며,  
Deferred 코루틴이 실행 완료되면 결괏값을 반환하고 호출부의 코루틴을 재개한다.

#### awaitAll

가변인자로 Deferred 타입의 객체를 받고 결과값들을 List로 만들어 반환한다.  
이 또한 결과가 수신될 때까지 호출부의 코루틴을 일시 중단한다.

```kotlin
public suspend fun <T> awaitAll(vararg deferreds: Deferred<T>): List<T>
```

Collection 인터페이스에 대한 확장 함수로도 제공한다.

```kotlin
val a = async<List<Int>> { listOf(1) }
val b = async<List<Int>> { listOf(2, 3) }
val result = listOf(a, b).awaitAll()
```

# withContext

```kotlin
public suspend fun <T> withContext(
    context: CoroutineContext,
    block: suspend CoroutineScope.() -> T
): T
```

block 람다식을 실행하고 완료되면 그 결과를 반환한다.   
async-await 쌍을 연속적으로 실행했을 때와 매우 유사하다.

차이점은 withContext는 실행 중이던 코루틴을 그대로 유지한 채로 코루틴의 실행환경만 변경해 작업을 처리하고,  
async-await 쌍은 새로운 코루틴을 생성해 작업을 처리한다.

```kotlin
// withContext
name()
withContext(Dispatchers.IO) {
    name()
}
```

```kotlin
// 결과
[main @coroutine #1]
[DefaultDispatcher - worker - 1 @coroutine #1]
```

스레드는 다르고, 코루틴은 똑같음

---

```kotlin
// async-await
name()
async(Dispatchers.IO) {
    name()
}
```

```kotlin
//결과
[main @coroutine #1]
[DefaultDispatcher - worker - 1 @coroutine #2]
```

스레드, 코루틴 둘 다 다름

### withContext 사용 시 주의점

withContext 함수는 새로운 코루틴을 만들지 않기 때문에 하나의 코루틴에서 withContext 함수가 여러 번 호출되면 순차적으로 실행된다.  
즉, 복수의 독립적인 작업이 병렬로 실행되야 하는 상황에 withContext를 사용할 경우 성능에 문제를 일으킬 수 있다.

```kotlin
val time = measureTimeMillis {
    withContext(Dispatchers.IO) {
        delay(2_000)
    }
    withContext(Dispatchers.IO) {
        delay(2_000)
    }
}
println("took $time ms")
// 결과 : took 4017 ms
```
