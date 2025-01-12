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


 
