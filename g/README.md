# 구조화된 동시성

구조화된 동시성(Structured Concurrency) 원칙 : 비동기 작업을 구조화함으로써 안정적이고 예측할 수 있게 만드는 원칙.  
코루틴은 부모-자식 관계로 구조화함으로써 보다 안전하게 관리되고 제어될 수 있도록 한다.

## 구조화된 코루틴 특징

1. 부모 코루틴 실행 환경이 자식 코루틴에게 상속 (Job 제외)
2. 부모 코루틴이 취소되면 자식 코루틴도 취소
3. 부모 코루틴은 자식 코루틴이 완료될 때까지 대기
4. CoroutineScope를 사용해 범위 제한 가능

## 실행 환경 상속

기본적으로는 CoroutineContext가 자식에게 전달된다.

```kotlin
val coroutineContext = newSingleThreadContext("My Thread") + CoroutineName("CoroutineA")
launch(coroutineContext) {
    name()
    launch {
        name()
    }
}
```

```kotlin
// 결과 -> 스레드 이름과 코루틴 이름이 같다
[My Thread @CoroutineA #2]
[My Thread @CoroutineA #3]
```

#### 실행 환경 상속 - 예외1 : 덮어씌우기

```kotlin
val coroutineContext = newSingleThreadContext("My Thread") + CoroutineName("ParentCoroutine")
launch(coroutineContext) {
    name()
    launch(CoroutineName("ChildCoroutine")) { // 새로운 CoroutineContext 전달
        name()
    }
}
```

```kotlin
// 결과
[My Thread @ParentCoroutine #2]
[My Thread @ChildCoroutine #3]
```

#### 실행 환경 상속 - 예외2 : 상속되지 않는 Job

Job 객체를 부모 코루틴으로부터 상속받게 되면 개별 코루틴의 제어가 어려워지기 때문에  
모든 코루틴 빌더 함수(launch, async)는 호출 때마다 Job 객체를 새롭게 생성한다.

```kotlin
val runBlockingJob = coroutineContext[Job]
launch {
    val launchJob = coroutineContext[Job]
    println(runBlockingJob == launchJob) // false
}
```

#### 구조화에 사용되는 Job

Job객체는 새로 생성되지만 `parent` 프로퍼티를 통해 부모 코루틴의 Job 객체에 대한 참조를 가진다.  
또한 부모 Job 객체는 Sequence 타입의 `children` 프로퍼티를 통해 자식 코루틴에 대한 참조를 가진다.  
즉, Job 객체는 양방향 참조를 가진다.

```kotlin
val parentJob = coroutineContext[Job]
launch {
    val childJob = coroutineContext[Job]
    println(parentJob?.children?.contains(childJob)) // true
    println(childJob?.parent == parentJob) // true
}
```

## 부모 코루틴이 취소되면 자식 코루틴도 취소

코루틴 구조화의 특징

1. 코루틴으로 취소가 요청되면 자식 코루틴으로 전파
2. 부모 코루틴은 모든 자식 코루틴이 실행 완료돼야 완료될 수 있다

## CoroutineScope

자신의 범위 내에서 생성된 코루틴들에게 실행 환경을 제공, 관리

CoroutineContext를 제공받으면서 생성됨

```kotlin
public fun CoroutineScope(context: CoroutineContext): CoroutineScope =
    ContextScope(if (context[Job] != null) context else context + Job())
```

---

```kotlin
일반적으로 Job 객체는 코루틴 빌더 함수를 통해 생성되는 코루틴을 제어하는 데 사용되지만
        CoroutineScope 객체 또한 Job 객체를 통해 하위에 생성되는 코루틴을 제어한다 .
따라서 코루틴은 Job 객체를 갖지만 Job 객체가 꼭 코루틴이 아닐수 있다.
```

```kotlin
// 코루틴은 Job 객체를 가진다
val scope = CoroutineScope(Job())  // 부모 Job 생성
val job = scope.launch { }

// 꼭 Job 객체가 코루틴이 아닐 수도 있다. Job 객체는 독립적으로 생성될 수 있다.
val job = Job()
```

## Job의 부모를 명시적으로 설정하기

```kotlin
public fun Job(parent: Job? = null): CompletableJob = JobImpl(parent)
```

`Job()` 을 통해 객체를 생성할 경우 parent 프로퍼티가 null이 돼 부모가 없는 루트 Job이 생성된다.

아래 방식대로 코드를 만들면 구조화가 끊어진다.

```kotlin
val newJob = Job()
println("coroutine1")
launch(CoroutineName("Coroutine2") + newJob) {
    delay(10)
    println("coroutine2")
}
```

```kotlin
// 출력
coroutine1
```

구조화를 꺠지 않도록 Job() 인자로 부모 코루틴의 Job 객체를 넘기면 된다.

```kotlin
val newJob = Job(coroutineContext[Job])
println("coroutine1")
launch(CoroutineName("Coroutine2") + newJob) {
    delay(10)
    println("coroutine2")
}
```

```kotlin
// 출력
coroutine1
coroutine2
```

## Job은 자동으로 실행 완료되지 않는다

Job 생성 함수로 생성된 객체는 자식 코루틴이 모두 실행 완료되더라도 자동으로 실행 완료되지 않는다.  
명시적으로 complete를 호출 해줘야한다.

```kotlin
val newJob = Job(coroutineContext[Job])
launch(CoroutineName("Coroutine2") + newJob) { ... }
newJob.complete() // 명시적 호출
```

## runBlocking과 launch 차이

- runBlocking : 호출부의 스레드를 차단
- launch : 차단 X

하위 runBlocking이 실행될 동안 호출부 스레드를 차단하기 때문에 1->2 순서가 된다.  
다만, 이는 스레드 블로킹과는 다르다. 스레드 블로킹은 스레드가 어떤 작업에도 사용할 수 없도록 차단되는 것을 의미하고,
runBlocking 함수의 차단은 다른 작업이 스레드를 사용할 수 없음을 의미한다.

```kotlin
runBlocking {
    runBlocking {
        delay(10)
        println("1")
    }
    println("2")
}
```

```kotlin
// runBlocking 결과
1
2
```

반면 launch는 호출부의 스레드를 차단하지 않기 때문에 2->1 순서로 출력이 된다

```kotlin
// launch
runBlocking {
    launch {
        delay(10)
        println("1")
    }
    println("2")
}
```

```kotlin
// launch일 때 결과
2
1
```
