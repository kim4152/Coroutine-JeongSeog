# CoroutineContext

withContext, launch, async 함수는 매개변수로 CoroutineContext 타입을 가진다.
이 객체는 코루틴을 실행하고 관리하는 데 핵심적인 역할을 하며, 코루틴의 실행과 관련된 모든 설정은 CoroutineContext 객체를 통해 이뤄진다.

#### CoroutineContext의 구성요소

1. CoroutineName : 코루틴의 이름 설정
2. CoroutineDispatcher : 코투린을 스레드에 할당해 실행
3. Job : 코루틴의 추상체로 코루틴을 조작하는 데 사용
4. CoroutineExceptionsHandler : 코루틴에서 발생한 예외를 처리

CoroutineContext 객체는 아래처럼 키-값 쌍으로 각 구성 요소를 관리한다.  
각 구성요소는 고유한 키를 가지며, 키에 대해 중복된 값은 허용되지 않는다. (객체를 한 개씩만 가질 수 있다. 나중에 추가된 구성 요소가 이전의 값을 덮어 씌운다.)

| 키                           | 값                         |
|-----------------------------|---------------------------|
| CoroutineName 키             | CoroutineName 객체          |
| CoroutineDispatcher 키       | CoroutineDispatcher 객체    |
| Job 키                       | Job 객체                    |
| CoroutineExceptionHandler 키 | CoroutineExceptionHandler |

### CoroutineContext 구성하기

`+` 연산자를 사용해 구성할 수 있다.

```kotlin
val coroutineContext: CoroutineContext = Dispatchers.IO + CoroutineName("MyCoroutine")

launch(coroutineContext) {
    println("name: ${coroutineContext[CoroutineName]}")
    println("dispatcher: ${coroutineContext[CoroutineDispatcher]}")
    println("job: ${coroutineContext[Job]}")
    println("exceptionHandler: ${coroutineContext[CoroutineExceptionHandler]}")
}
```

### CoroutineContext 구성 요소 제거하기

```kotlin
val coroutineContext: CoroutineContext = Dispatchers.IO + CoroutineName("MyCoroutine")
val newCoroutineContext = coroutineContext.minusKey(CoroutineName)
```

새로운 CoroutineContext 반환
