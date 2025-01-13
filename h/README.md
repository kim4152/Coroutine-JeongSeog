# 예외 처리

코루틴 실행 중 예외가 발생하면 해당 코루틴은 취소되고 부모 코루틴으로 예외가 전파된다.  
만약 부모 코루틴에서 적절히 처리되지 않으면 다시 부모 코루틴으로 루트 코루틴까지 가게 된다.

코루틴이 예외를 전파받아 취소되면 해당 코루틴만 취소되는 것이 아니라 하위에 있는 모든 코루틴에게 취소가 전파된다.

만약 루트 코루틴에서 조차 적절히 처리되지 않으면 모든 하위 코루틴이 취소될 수 있다.

## 예외 전파 제한하기

### 1. Job 객체를 사용한 예외 전파 제한

새로운 Job 객체를 만들어 구조화를 깨는 방식이다. 하지만 예외 전파를 제한하는 동시에 취소 전파도 제한하기 때문에 사용에 주의해야한다.

### 2. SuperVisorJob 객체를 사용한 예외 전파 제한

구조화를 깨지 않으면서 예외 전파를 제한할 수 있다.  
자식 코루틴으로부터 예외전파를 받지 않는 특수한 Job 객체이다.

Job 객체를 만드는 방법과 비슷하다. parent 인자 없이 사용하면 루트 Job으로 만들 수 있고, 인자로 Job 객체를 넘기면 부모 Job이 있는 SupervisorJob 객체를 만들 수 있다.

```kotlin
public fun SupervisorJob(parent: Job? = null): CompletableJob = SupervisorJobImpl(parent)
```

##### SupervisorJob 객체를 사용한 예외 전파 테스트

Coroutine3에서 예외가 발생되어도 Coroutine1에 전파되어 취소시키지만, Coroutin1은 supervisorJob으로 예외를 전파시키지 않는다.

```kotlin
runBlocking {
    val supervisorJob = SupervisorJob()
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
```

하지만 이 또한 SupervisorJob 객체가 runBlocking이 호출돼 만들어진 Job 객체와의 구조화를 깨고있다.

### 구조화를 깨지 않고 SupervisorJob 사용하기

기존 코드의 SuperVisorJob 객체의 parent 인자에 부모 Job 객체를 넘기면 된다.

```kotlin
val supervisorJob = SupervisorJob(this.coroutineContext[Job])
```

또한 마지막에 complete() 함수를 명시적으로 호출해줘야 한다.

### SupervisorJob을 CoroutineScope와 함께 사용하기

CoroutineScope의 CoroutineContext에 SupervisorJob 객체가 설정된다면 CoroutineScope의 자식 코루틴에서 발생하는 예외가 다른 자식 코루틴으로 전파되지 않는다.

```kotlin
runBlocking<Unit> {
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
```

```kotlin
// 출력
333
Exception in thread "DefaultDispatcher-worker-1" java . lang . Exception : exception
```

### SupervisorJob 사용시 주의 할 점

코루틴 빌더 함수의 context 인자에 SupurvisorJob()을 넘기고,   
코루틴 빌더 함수가 호출돼 생성되는 코루틴의 하위 자식 코루틴들을 생성하는 것을 조심해야한다.

```kotlin
runBlocking {
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
```

context 인자에 Job 객체가 입력될 경우 해당 Job 객체를 부모로 하는 새로운 Job 객체를 만들기 때문이다.

즉, launch 함수에 SupervisorJob()을 인자로 넘기면  
`SupervisorJob()을 통해 만들어지는 SupervisorJob 객체`를   
부모로하는 새로운 Job 객체가 만들어진다.

아래와 같은 구조가 된다.

//todo : 구조 274

SupervisorJob은 강력한 예외 전파 방지 도구이지만 Job 계층 구조의 어떤 위치에 있어야 하는지 충분히 고민하고 사용해야한다.

### 3. SuperVisorScope를 사용한 예외 전파 제한

```kotlin
public suspend fun <R> supervisorScope(block: suspend CoroutineScope.() -> R): R
```

이 함수는 `SupervisorJob` 객체를 가진 CoroutineScope 객체를 생성하며,  
이 `SupervisorJob` 객체는 이 함수를 호출한 Job 객체를 부모로 가진다.

## 예외 처리

### CoroutineExceptionHandler

람다식에 예외가 발생했을 때 어떤 종작을 할지 입력해 예외를 처리할 수 있다.

```kotlin
public inline fun CoroutineExceptionHandler(crossinline handler: (CoroutineContext, Throwable) -> Unit): CoroutineExceptionHandler
```

CoroutineExceptionHandler 객체는 CoroutineContext 객체의 구성 요소로 포함될 수 있다.

```kotlin
runBlocking {
    val exceptionHandler = CoroutineExceptionHandler { context, throwable ->
        println("exception : ${throwable}")
    }
    CoroutineScope(exceptionHandler).launch { }
}
```

CoroutineExceptionHandler 객체는 처리되지 않은 예외만 처리한다.   
자식 코루틴에서 부모 코루틴으로 예외를 전파하면 자식에서는 예외가 처리된 것으로 봐 자식 코루틴에 설정된 Handler는 동작하지 않는다.

아래 코드를 보면 coroutine1이 runBlocking으로 예외를 전파한다.    
`예외가 전파되면 예외를 처리한 것`으로 보기 때문에 CoroutineExceptionHandler는 동작하지 않는다.  
그리고 부모 코루틴인 runBlocking은 예외 처리가 안되어있기 때문에 오류 로그만 나오게 된다.

```kotlin
runBlocking {
    val exceptionHandler = CoroutineExceptionHandler { context, throwable ->
        println("exception : ${throwable}")
    }
    launch(CoroutineName("coroutine1") + exceptionHandler) {
        throw Exception("exception")
    }
    delay(1_000)
}
```

```kotlin
// 결과
Exception in thread "main" java . lang . Exception : exception
```

위 코드에서 알 수 있듯 여러 코루틴에 CoroutineExceptionHandler 객체가 설정돼 있더라도 마지막으로 예외를 전파받는 위치에 설정된  
핸들러 객체만 예외를 처리한다. 이런 특징 때문에 이 객체는 `공통 예외 처리기`로써 동작할 수 있다.

아래처럼 루트 Job과 함께 설정하면 Handler객체가 설정되는 위치에서 예외처리를 할 수 있게된다.

```kotlin
runBlocking {
    val context = Job() + CoroutineExceptionHandler { context, throwable ->
        println("exception : ${throwable}")
    }
    launch(CoroutineName("coroutine1") + context) {
        throw Exception("exception")
    }
    delay(1_000)
}
```

#### SupervisorJob과 CoroutineExceptionHandler

SupervisorJob 객체가 부모 Job으로 설정되면 자식으로부터 예외를 전파 받지 않지만,  
예외 내용은 전달 받기 때문에 SupervisorJob과 같이 사용해도 예외 처리가 된다.

```kotlin
val handler = CoroutineExceptionHandler { context, throwable ->
    println("exception : ${throwable}")
}
val supervisedScope = CoroutineScope(SupervisorJob() + handler)
```

### try-catch

try-catch문을 코루틴 빌더 함수에 사용하는 것에 주의해야한다.  
아래 코드는 코루틴에서 발생한 예외가 잡히지 않는다.

```kotlin
try {
    launch { throw Exception("") }
} catch (e: Exception) {
    println(e)
}
```

launch는 코루틴을 생성하는 데 사용되는 함수일 뿐, 람다식의 실행은 CoroutineDispatcher에 의해 스레드로 분배되는 시점에 일어나기 때문이다.  
즉, launch 함수 자체의 실행만 체크하며, 람다식은 예외 처리 대상이 아니다.

### async 예외처리

async 함수는 결괏값을 Deferred 객체로 감싸고 await 호출 시점에 결괏값을 노출한다.  
이런 특성 때문에 코루틴 실행중 예외가 발생해 결괏값이 없다면 await 호출 시 예외가 노출된다.

```kotlin
val handler = CoroutineExceptionHandler { context, throwable ->
    println("exception : ${throwable}")
}
val supervisedScope = CoroutineScope(SupervisorJob() + handler)
val result = supervisedScope.async {
    throw Exception("error")
    "async"
}
println(result.await())
```

위 코드는 CoroutineExceptionHandler로 예외 처리를 해준것 같지만, await 호출부에서 예외 처리가 될 수 있도록 해줘야 한다.

### 전파되지 않는 예외 CancellationException

CancellationException 예외가 발생해도 부모 코루틴으로 전파되지 않는다.  
cancel함수, witTimeOut 함수에서 이 예외를 사용하고 있다.  
