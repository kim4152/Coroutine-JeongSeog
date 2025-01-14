# 일시 중단 함수

일시 중단 함수는 suspend fun 키워드로 선언되는 함수로 일시 중단 지점을 포함할 수 있는 특별한 기능을 한다.

### 일시 중단 함수는 코루틴이 아니다

일시 중단 함수는 코루틴 내부에서 실행되는 코드 집합일 뿐이다.  
다음 코드에서 생성되는 코루틴은 runBlocking밖에 없다.

```kotlin
fun main() = runBlocking {
    delayAndPrint()
    delayAndPrint()
}
suspend fun delayAndPrint() {
    delay(1_000)
    println("Hello world")
}
```

### 일시 중단 함수를 코루틴처럼 사용하고 싶다면코루틴 빌더로 감싸야한다

```kotlin
fun main() = runBlocking {
    launch {
        delayAndPrint()
    }
}
```

### suspend 함수에서 이 함수를 호출한 코루틴의 CoroutineScope 객체에 접근할 수 없다.

```kotlin
// 불가 -> CoroutineScope 없음
suspend fun delayAndPrint() {
    launch { }
}
```

coroutineScope 함수를 사용하면 suspend 함수 내부에서 CoroutineScope 객체를 생성할 수 있다.    
coroutineScope 함수는 구조화를 깨지 않는 CoroutineScope 객체를 생성한다.

```kotlin
public suspend fun <R> coroutineScope(block: suspend CoroutineScope.() -> R): R
```

```kotlin
suspend fun delayAndPrint() = coroutineScope {
    launch { }
}
```

### coroutineScope 대신 supervisorScope를 사용하면 예외 처리까지 할 수 있다.

```kotlin
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
```

위 코드를 구조화 하면
//todo 315

